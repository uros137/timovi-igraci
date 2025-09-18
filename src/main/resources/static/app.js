// ==== helpers ====
const $ = s => document.querySelector(s);

// stanje
let curPage = 0;
let pageSize = 10;
let lastFilters = { position: "", teamId: "" };

// ==== TEAMS ====
async function loadTeams() {
  const r = await fetch("/api/teams");
  if (!r.ok) throw new Error(await r.text());
  const teams = await r.json();

  const tb = document.getElementById("teams-list");
  if (tb) {
    tb.innerHTML = "";
    teams.forEach(t => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${t.id}</td>
        <td>${t.name}</td>
        <td>${t.city ?? ""}</td>
        <td>
          <button onclick="editTeam(${t.id}, '${(t.name||"").replace(/'/g,"\\'")}', '${(t.city||"").replace(/'/g,"\\'")}')">Izmeni</button>
          <button onclick="deleteTeam(${t.id})">Obriši</button>
        </td>`;
      tb.appendChild(tr);
    });
  }

  const sel = document.getElementById("player-team");
  if (sel) {
    sel.innerHTML = "";
    teams.forEach(t => {
      const opt = document.createElement("option");
      opt.value = t.id;
      opt.textContent = t.name;
      sel.appendChild(opt);
    });
  }
}

async function addTeam() {
  const name = $("#team-name").value.trim();
  const city = $("#team-city").value.trim();
  if (!name) return alert("Unesi naziv tima");
  const r = await fetch("/api/teams", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, city })
  });
  if (!r.ok) return alert(await r.text());
  $("#team-name").value = "";
  $("#team-city").value = "";
  await loadTeams();
  await reloadPlayers();
}

async function editTeam(id, name, city) {
  const n = prompt("Naziv", name ?? "");
  if (n === null) return;
  const c = prompt("Grad", city ?? "");
  if (c === null) return;
  const r = await fetch(`/api/teams/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name: n, city: c })
  });
  if (!r.ok) return alert(await r.text());
  await loadTeams();
  await reloadPlayers();
}

async function deleteTeam(id) {
  if (!confirm("Obrisati tim?")) return;
  const r = await fetch(`/api/teams/${id}`, { method: "DELETE" });
  if (!r.ok) return alert(await r.text());
  await loadTeams();
  await reloadPlayers();
}

// ==== PLAYERS ====
async function loadPlayers(page, size, position = "", teamId = "") {
  const url = `/api/players?page=${page}&size=${size}`
    + (position ? `&position=${encodeURIComponent(position)}` : "")
    + (teamId ? `&teamId=${encodeURIComponent(teamId)}` : "");
  const r = await fetch(url);
  if (!r.ok) return alert(await r.text());
  const data = await r.json();

  const tb = document.getElementById("players-list");
  tb.innerHTML = "";
  data.content.forEach(p => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${p.id}</td>
      <td>${p.fullName}</td>
      <td>${p.age ?? ""}</td>
      <td>${p.position ?? ""}</td>
      <td>${p.team ? p.team.name : ""}</td>
      <td>
        <button onclick="editPlayer(${p.id})">Izmeni</button>
        <button onclick="deletePlayer(${p.id})">Obriši</button>
      </td>`;
    tb.appendChild(tr);
  });

  const info = document.getElementById("players-page-info");
  if (info) info.textContent = `Strana ${data.number + 1} / ${Math.max(1, data.totalPages)} • Ukupno ${data.totalElements}`;
  $("#prev-btn").disabled = data.number === 0;
  $("#next-btn").disabled = data.number + 1 >= data.totalPages;

  $("#page-input") && ($("#page-input").value = data.number + 1);
  $("#size-input") && ($("#size-input").value = data.size);

  curPage = data.number;
  pageSize = data.size;
  lastFilters = { position, teamId };
}

async function reloadPlayers() {
  const pos = $("#filter-position").value.trim();
  const tid = $("#filter-teamid").value.trim();
  await loadPlayers(curPage, pageSize, pos, tid);
}

async function addPlayer() {
  const fullName = $("#player-name").value.trim();
  if (!fullName) return alert("Ime je obavezno");
  const age = parseInt($("#player-age").value || "0", 10) || null;
  const position = $("#player-position").value;
  const teamId = $("#player-team").value;

  const r = await fetch(`/api/players?teamId=${teamId}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ fullName, age, position })
  });
  if (!r.ok) return alert(await r.text());

  $("#player-name").value = "";
  $("#player-age").value = "";
  await reloadPlayers();
}

async function editPlayer(id) {
  const p = await (await fetch(`/api/players/${id}`)).json();
  const name = prompt("Ime i prezime", p.fullName); if (name === null) return;
  const age  = prompt("Godine", p.age ?? "");        if (age === null) return;
  const pos  = prompt("Pozicija (GK/DF/MF/FW)", p.position ?? ""); if (pos === null) return;
  let teamId = p.team ? p.team.id : null;
  const maybe = prompt("Team ID (Enter za isto)", teamId ?? "");
  if (maybe !== null && maybe.trim() !== "") teamId = parseInt(maybe, 10);

  const body = { fullName: name, age: age === "" ? null : parseInt(age, 10), position: pos };
  if (teamId) body.team = { id: teamId };
  const r = await fetch(`/api/players/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  if (!r.ok) return alert(await r.text());
  await reloadPlayers();
}

async function deletePlayer(id) {
  if (!confirm("Obrisati igrača?")) return;
  const r = await fetch(`/api/players/${id}`, { method: "DELETE" });
  if (!r.ok) return alert(await r.text());
  await reloadPlayers();
}

// ==== FILTERI, PAGE, SIZE ====
$("#apply-filters")?.addEventListener("click", async () => {
  curPage = 0;
  const pageInput = parseInt($("#page-input").value || "1", 10);
  const sizeInput = parseInt($("#size-input").value || "10", 10);
  curPage = Math.max(0, pageInput - 1);
  pageSize = Math.max(1, sizeInput);

  const pos = $("#filter-position").value.trim();
  const tid = $("#filter-teamid").value.trim();
  await loadPlayers(curPage, pageSize, pos, tid);
});

$("#prev-btn")?.addEventListener("click", async () => {
  if (curPage > 0) {
    curPage--;
    await reloadPlayers();
  }
});

$("#next-btn")?.addEventListener("click", async () => {
  curPage++;
  await reloadPlayers();
});

$("#page-input")?.addEventListener("keydown", e => { if (e.key === "Enter") $("#apply-filters").click(); });
$("#size-input")?.addEventListener("keydown", e => { if (e.key === "Enter") $("#apply-filters").click(); });

// ==== INIT ====
document.addEventListener("DOMContentLoaded", async () => {
  // >>>> fix: zakači dugmad
  document.getElementById("add-team")?.addEventListener("click", addTeam);
  document.getElementById("add-player")?.addEventListener("click", addPlayer);

  if ($("#page-input")) $("#page-input").value = "1";
  if ($("#size-input")) $("#size-input").value = "10";

  await loadTeams();
  await loadPlayers(0, 10);
});

// expose
window.editTeam = editTeam;
window.deleteTeam = deleteTeam;
