// ERS Frontend — minimal SPA

const API = '';

// State
let token = localStorage.getItem('ers_token');
let currentUser = null;

// ==================== INIT ====================

document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        fetchMe();
    } else {
        showAuth();
    }
    setupEventListeners();
});

function setupEventListeners() {
    // Auth tabs
    document.querySelectorAll('#auth-tabs .tab').forEach(tab => {
        tab.addEventListener('click', () => switchAuthTab(tab.dataset.tab));
    });

    // Forms
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('reimb-form').addEventListener('submit', handleSubmitReimb);
    document.getElementById('edit-form').addEventListener('submit', handleEditReimb);
    document.getElementById('edit-cancel').addEventListener('click', () => {
        document.getElementById('edit-modal').close();
    });

    // Logout
    document.getElementById('logout-btn').addEventListener('click', handleLogout);

    // Filters
    document.getElementById('filter-status').addEventListener('change', loadReimbursements);
    document.getElementById('filter-department').addEventListener('change', loadReimbursements);
}

// ==================== AUTH ====================

function switchAuthTab(tab) {
    document.querySelectorAll('#auth-tabs .tab').forEach(t => t.classList.remove('active'));
    document.querySelector(`#auth-tabs .tab[data-tab="${tab}"]`).classList.add('active');
    document.getElementById('login-form').classList.toggle('hidden', tab !== 'login');
    document.getElementById('register-form').classList.toggle('hidden', tab !== 'register');

    if (tab === 'register') loadDepartmentsForRegister();
}

async function loadDepartmentsForRegister() {
    const sel = document.getElementById('reg-department');
    if (sel.options.length > 0) return; // already loaded
    try {
        const res = await fetch(API + '/api/departments');
        const depts = await res.json();
        depts.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = d.name;
            sel.appendChild(opt);
        });
    } catch (e) {
        console.error('Failed to load departments', e);
    }
}

async function handleLogin(e) {
    e.preventDefault();
    clearErrors();
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;

    try {
        const res = await fetch(API + '/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Login failed');

        token = data.token;
        localStorage.setItem('ers_token', token);
        currentUser = data.user;
        showDashboard();
    } catch (err) {
        document.getElementById('login-error').textContent = err.message;
    }
}

async function handleRegister(e) {
    e.preventDefault();
    clearErrors();
    const username = document.getElementById('reg-username').value.trim();
    const password = document.getElementById('reg-password').value;
    const firstName = document.getElementById('reg-firstname').value.trim();
    const lastName = document.getElementById('reg-lastname').value.trim();
    const departmentId = parseInt(document.getElementById('reg-department').value);

    try {
        const res = await fetch(API + '/api/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password, firstName, lastName, departmentId })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Registration failed');

        document.getElementById('register-error').textContent = 'Registered! Please log in.';
        document.getElementById('register-error').style.color = '#2ecc71';
        setTimeout(() => switchAuthTab('login'), 1200);
    } catch (err) {
        document.getElementById('register-error').textContent = err.message;
    }
}

async function handleLogout() {
    try {
        await fetch(API + '/api/logout', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token }
        });
    } catch (e) { /* ignore */ }
    token = null;
    currentUser = null;
    localStorage.removeItem('ers_token');
    showAuth();
}

async function fetchMe() {
    try {
        const res = await fetch(API + '/api/me', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!res.ok) throw new Error('Session expired');
        currentUser = await res.json();
        showDashboard();
    } catch (e) {
        token = null;
        localStorage.removeItem('ers_token');
        showAuth();
    }
}

// ==================== UI STATE ====================

function showAuth() {
    document.getElementById('auth-section').classList.remove('hidden');
    document.getElementById('dashboard-section').classList.add('hidden');
}

function showDashboard() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('dashboard-section').classList.remove('hidden');

    const roleBadge = `<span class="role-badge role-${currentUser.role.toLowerCase()}">${currentUser.role}</span>`;
    document.getElementById('user-info').innerHTML =
        `Welcome, ${currentUser.firstName} ${currentUser.lastName} (${currentUser.username}) ${roleBadge}`;

    if (currentUser.role === 'MANAGER') {
        document.getElementById('filter-department').classList.remove('hidden');
        loadDepartmentsForFilter();
    }

    loadReimbursements();
}

// ==================== REIMBURSEMENTS ====================

async function handleSubmitReimb(e) {
    e.preventDefault();
    clearErrors();
    const amount = parseFloat(document.getElementById('reimb-amount').value);
    const type = document.getElementById('reimb-type').value;
    const description = document.getElementById('reimb-desc').value.trim();

    try {
        const res = await fetch(API + '/api/reimbursements', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ amount, type, description })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Submission failed');

        document.getElementById('submit-success').textContent = 'Reimbursement submitted successfully!';
        document.getElementById('reimb-form').reset();
        setTimeout(() => {
            document.getElementById('submit-success').textContent = '';
        }, 3000);
        loadReimbursements();
    } catch (err) {
        document.getElementById('submit-error').textContent = err.message;
    }
}

async function loadReimbursements() {
    const status = document.getElementById('filter-status').value;
    let url = API + '/api/reimbursements?';
    if (status) url += 'status=' + status + '&';

    const deptSel = document.getElementById('filter-department');
    if (currentUser.role === 'MANAGER' && deptSel.value) {
        url += 'departmentId=' + deptSel.value;
    }

    try {
        const res = await fetch(url, {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!res.ok) throw new Error('Failed to load');
        const reimbs = await res.json();
        renderReimbursements(reimbs);
    } catch (err) {
        console.error(err);
    }
}

function renderReimbursements(reimbs) {
    const tbody = document.getElementById('reimb-tbody');
    const empty = document.getElementById('no-reimbs');
    tbody.innerHTML = '';

    if (reimbs.length === 0) {
        empty.classList.remove('hidden');
        document.getElementById('reimb-table').classList.add('hidden');
        return;
    }

    empty.classList.add('hidden');
    document.getElementById('reimb-table').classList.remove('hidden');

    reimbs.forEach(r => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${r.id}</td>
            <td>$${r.amount.toFixed(2)}</td>
            <td>${r.type}</td>
            <td>${escapeHtml(r.description)}</td>
            <td><span class="status-badge status-${r.status}">${r.status}</span></td>
            <td>${r.author.firstName} ${r.author.lastName}</td>
            <td>${formatDate(r.submittedAt)}</td>
            <td>${formatDate(r.resolvedAt)}</td>
            <td>${r.resolver ? r.resolver.firstName + ' ' + r.resolver.lastName : '-'}</td>
            <td class="actions-cell">${buildActions(r)}</td>
        `;
        tbody.appendChild(tr);
    });
}

function buildActions(r) {
    let html = '';
    // Edit: only author can edit PENDING
    if (r.status === 'PENDING' && r.author.id === currentUser.id) {
        html += `<button class="btn-secondary btn-sm" onclick="openEdit(${r.id}, ${r.amount}, '${r.type}', '${escapeAttr(r.description)}')">Edit</button> `;
    }
    // Resolve: only managers can approve/deny PENDING
    if (r.status === 'PENDING' && currentUser.role === 'MANAGER') {
        html += `<button class="btn-approve btn-sm" onclick="resolveReimb(${r.id}, 'APPROVED')">Approve</button> `;
        html += `<button class="btn-deny btn-sm" onclick="resolveReimb(${r.id}, 'DENIED')">Deny</button>`;
    }
    return html;
}

async function resolveReimb(id, status) {
    if (!confirm(`Are you sure you want to ${status.toLowerCase()} reimbursement #${id}?`)) return;
    try {
        const res = await fetch(API + `/api/reimbursements/${id}/resolve`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ status })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Resolution failed');
        loadReimbursements();
    } catch (err) {
        alert(err.message);
    }
}

// ==================== EDIT MODAL ====================

function openEdit(id, amount, type, description) {
    document.getElementById('edit-id').value = id;
    document.getElementById('edit-amount').value = amount;
    document.getElementById('edit-type').value = type;
    document.getElementById('edit-desc').value = description;
    document.getElementById('edit-error').textContent = '';
    document.getElementById('edit-modal').showModal();
}

async function handleEditReimb(e) {
    e.preventDefault();
    const id = parseInt(document.getElementById('edit-id').value);
    const amount = parseFloat(document.getElementById('edit-amount').value);
    const type = document.getElementById('edit-type').value;
    const description = document.getElementById('edit-desc').value.trim();

    try {
        const res = await fetch(API + `/api/reimbursements/${id}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            },
            body: JSON.stringify({ amount, type, description })
        });
        const data = await res.json();
        if (!res.ok) throw new Error(data.error || 'Edit failed');
        document.getElementById('edit-modal').close();
        loadReimbursements();
    } catch (err) {
        document.getElementById('edit-error').textContent = err.message;
    }
}

// ==================== HELPERS ====================

function clearErrors() {
    document.querySelectorAll('.form-error').forEach(el => el.textContent = '');
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    const d = new Date(dateStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function escapeAttr(str) {
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

async function loadDepartmentsForFilter() {
    const sel = document.getElementById('filter-department');
    if (sel.options.length > 1) return; // already loaded
    try {
        const res = await fetch(API + '/api/departments');
        const depts = await res.json();
        depts.forEach(d => {
            const opt = document.createElement('option');
            opt.value = d.id;
            opt.textContent = d.name;
            sel.appendChild(opt);
        });
    } catch (e) {
        console.error('Failed to load departments', e);
    }
}
