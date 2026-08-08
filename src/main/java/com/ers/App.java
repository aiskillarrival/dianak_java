package com.ers;

import com.ers.controller.AuthController;
import com.ers.controller.DepartmentController;
import com.ers.controller.ReimbursementController;
import com.ers.dao.DepartmentDao;
import com.ers.dao.ReimbursementDao;
import com.ers.dao.UserDao;
import com.ers.service.DepartmentService;
import com.ers.service.ReimbursementService;
import com.ers.service.UserService;
import com.ers.util.DatabaseUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final int PORT = 7000;

    public static void main(String[] args) {
        // Trigger schema initialization
        try { DatabaseUtil.getConnection().close(); } catch (java.sql.SQLException ignored) {}

        // Instantiate DAOs
        DepartmentDao departmentDao = new DepartmentDao();
        UserDao userDao = new UserDao();
        ReimbursementDao reimbursementDao = new ReimbursementDao();

        // Instantiate services
        DepartmentService departmentService = new DepartmentService(departmentDao);
        UserService userService = new UserService(userDao, departmentService);
        ReimbursementService reimbursementService = new ReimbursementService(reimbursementDao, userService);

        // JSON serializer
        Gson gson = new GsonBuilder().serializeNulls().create();

        // Instantiate controllers
        AuthController authController = new AuthController(userService, gson);
        DepartmentController departmentController = new DepartmentController(departmentService);
        ReimbursementController reimbursementController = new ReimbursementController(
                reimbursementService, userService, gson);

        // Build Javalin app
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });
            config.router.apiBuilder(() -> {
                // Auth routes
                io.javalin.apibuilder.ApiBuilder.path("/api", () -> {
                    io.javalin.apibuilder.ApiBuilder.post("/register", authController::register);
                    io.javalin.apibuilder.ApiBuilder.post("/login", authController::login);
                    io.javalin.apibuilder.ApiBuilder.post("/logout", authController::logout);
                    io.javalin.apibuilder.ApiBuilder.get("/me", authController::me);

                    // Department routes
                    io.javalin.apibuilder.ApiBuilder.get("/departments", departmentController::list);

                    // Reimbursement routes
                    io.javalin.apibuilder.ApiBuilder.post("/reimbursements", reimbursementController::submit);
                    io.javalin.apibuilder.ApiBuilder.get("/reimbursements", reimbursementController::list);
                    io.javalin.apibuilder.ApiBuilder.get("/reimbursements/{id}", reimbursementController::getById);
                    io.javalin.apibuilder.ApiBuilder.put("/reimbursements/{id}", reimbursementController::update);
                    io.javalin.apibuilder.ApiBuilder.patch("/reimbursements/{id}/resolve", reimbursementController::resolve);
                });
            });
        });

        // Global error handler
        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled error", e);
            ctx.status(500).json(java.util.Map.of("error", "Internal server error"));
        });

        app.start(PORT);
        log.info("ERS server started on http://localhost:{}", PORT);
    }
}
