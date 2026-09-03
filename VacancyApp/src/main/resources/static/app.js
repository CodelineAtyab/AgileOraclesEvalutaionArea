'use strict';

const vacanciesEl = document.getElementById('vacancies');
const statusEl = document.getElementById('status');
const applyDialog = document.getElementById('applyDialog');
const applyForm = document.getElementById('applyForm');
const applyTitle = document.getElementById('applyTitle');
const applyMsg = document.getElementById('applyMsg');

let currentJobId = null;

function fmt(ts) {
    if (!ts) return '—';
    const d = new Date(ts);
    return isNaN(d) ? ts : d.toLocaleString();
}

function escapeHtml(s) {
    return String(s ?? '').replace(/[&<>"']/g, c =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

async function loadVacancies() {
    statusEl.textContent = 'Loading…';
    vacanciesEl.innerHTML = '';
    try {
        const res = await fetch('/vacancies/available');
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const jobs = await res.json();
        if (!jobs.length) {
            statusEl.textContent = 'No open vacancies right now.';
            return;
        }
        statusEl.textContent = jobs.length + ' open vacanc' + (jobs.length === 1 ? 'y' : 'ies') + '.';
        for (const job of jobs) {
            vacanciesEl.appendChild(renderCard(job));
        }
    } catch (e) {
        statusEl.textContent = 'Could not load vacancies: ' + e.message;
    }
}

function renderCard(job) {
    const card = document.createElement('div');
    card.className = 'card';
    card.innerHTML =
        '<h3>' + escapeHtml(job.title) + '</h3>' +
        '<div class="dept">' + escapeHtml(job.department || 'General') + '</div>' +
        '<p class="desc">' + escapeHtml(job.description) + '</p>' +
        '<div class="meta">Expires: ' + fmt(job.expiresAt) + '</div>';
    const btn = document.createElement('button');
    btn.className = 'btn';
    btn.textContent = 'Apply';
    btn.addEventListener('click', () => openApply(job));
    card.appendChild(btn);
    return card;
}

function openApply(job) {
    currentJobId = job.jobId;
    applyTitle.textContent = 'Apply — ' + job.title;
    applyMsg.textContent = '';
    applyForm.reset();
    applyDialog.showModal();
}

document.getElementById('applyCancel').addEventListener('click', () => applyDialog.close());

applyForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(applyForm).entries());
    applyMsg.textContent = 'Submitting…';
    try {
        const res = await fetch('/vacancies/' + currentJobId + '/applications', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const payload = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(payload.message || ('HTTP ' + res.status));
        applyMsg.className = 'msg ok';
        applyMsg.textContent = 'Application submitted! (id ' + payload.applicationId + ')';
        setTimeout(() => applyDialog.close(), 1200);
    } catch (err) {
        applyMsg.className = 'msg err';
        applyMsg.textContent = err.message;
    }
});

const createForm = document.getElementById('createForm');
const createMsg = document.getElementById('createMsg');

createForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(createForm).entries());
    if (!data.department) delete data.department;
    createMsg.textContent = 'Creating…';
    try {
        const res = await fetch('/vacancies', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const payload = await res.json().catch(() => ({}));
        if (!res.ok) throw new Error(payload.message || ('HTTP ' + res.status));
        createMsg.className = 'msg ok';
        createMsg.textContent = 'Created vacancy #' + payload.jobId;
        createForm.reset();
        loadVacancies();
    } catch (err) {
        createMsg.className = 'msg err';
        createMsg.textContent = err.message;
    }
});

document.getElementById('refreshBtn').addEventListener('click', loadVacancies);
loadVacancies();
