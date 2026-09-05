# CytrilDuel

Plugin duel/party PvP untuk Paper/Spigot 1.20.x.

## ✨ Fitur

**Duel & Party**
- `/duel` membuka GUI daftar pemain online (kepala skin asli) untuk ditantang.
- Klik kepala pemain → GUI pilih arena/maps (1–6, tiap arena bisa punya kit sendiri) → ajakan duel terkirim.
- Notifikasi ajakan duel & party muncul di chat dengan tombol **[TERIMA]/[TOLAK]** yang bisa diklik, atau lewat perintah `/duel accept|deny <pemain>` dan `/party accept|deny <pemain>`.
- `/duel cancel` membatalkan ajakan yang kamu kirim; kedua pihak (pengirim & penerima) bisa membatalkan.
- Party = grup privat untuk duel bersama (bukan tim anti-PvP). Ketua party yang menjalankan `/duel` akan otomatis membawa seluruh anggota party bertarung/ikut TP ke arena yang dipilih.
- Party dibubarkan otomatis jika ketua keluar/logout; anggota lain otomatis keluar dari party. Saat login ulang, pemain tidak berada di party manapun sampai diundang lagi.
- Item lobby otomatis diberikan saat join:
  - **Slot 1 (kiri, Gold Ingot):** menu party (undang pemain / keluar party).
  - **Slot 5 (tengah, Kepala Pemain):** profil & statistik (menang/kalah/kill).
  - **Slot 9 (kanan, Nether Star):** pilih mode/arena (termasuk mode **Global/FFA**).

**Arena & Kit**
- Admin bisa membuat banyak arena dengan kit & mode berbeda:
  `/duel setarena <nama> <lebar> <kit> <duel|global>`
- Tiap arena hanya mengizinkan kit tertentu (dicontohkan: arena 1 khusus `kit1`, arena 2 khusus `kit2`, dst — nama kit bisa dikustom bebas).
- Mode **duel**: party vs party, privat, map dikunci selama dipakai.
- Mode **global**: FFA, banyak pemain campur di 1 arena yang sama, party diabaikan (semua individu saling menyerang).
- `/duel setkit <nama>` menyimpan kit dari inventory admin saat itu juga (armor + isi inventory).
- `/duel setlobby` menetapkan titik lobby PvP tempat pemain kembali setelah duel/mati.

**Gameplay**
- Saat mati dalam duel, pemain **tidak langsung kembali ke lobby** — otomatis menjadi spectator dengan batas gerak (default 100 blok dari titik tengah arena, bisa diubah admin lewat `/duel spectatorradius <angka>`).
- Ketika hanya tersisa 1 pihak (mode duel) atau 1 pemain (mode global), pertandingan otomatis selesai, seluruh pemain (menang & kalah) dikembalikan ke lobby.
- Statistik menang/kalah/kill tersimpan permanen per pemain (`stats.yml`).
- PvP hanya aktif antar pemain yang sedang berada dalam sesi duel yang sama — aman dari serangan iseng di lobby.
- Kelaparan (food level) dinonaktifkan di luar sesi duel.

**PlaceholderAPI (bisa "dicampur" ke plugin lain)**
Selain statistik individu, plugin ini menyediakan **leaderboard cache** yang di-refresh otomatis (default tiap 60 detik, bisa diatur `leaderboard-refresh-seconds` di config.yml) dan diekspos lewat PlaceholderAPI — sehingga bisa langsung dipakai di plugin display lain seperti **HolographicDisplays**, **DeluxeMenus**, **TAB**, **Leaderboardz**, **DecentHolograms**, dll, tanpa perlu koding tambahan.

Placeholder statistik per-pemain:
```
%cytril_duel_win%    -> jumlah kemenangan pemain
%cytril_duel_deat%   -> jumlah kekalahan pemain
%cytril_duel_kill%   -> jumlah kill pemain
```

Placeholder leaderboard (top ranking, tidak bergantung siapa yang melihat):
```
%cytril_duel_top_win_name_1%    -> nama pemain peringkat 1 menang
%cytril_duel_top_win_value_1%   -> jumlah menang peringkat 1
%cytril_duel_top_kill_name_1%   -> nama pemain peringkat 1 kill
%cytril_duel_top_kill_value_1%  -> jumlah kill peringkat 1
%cytril_duel_top_deat_name_1%   -> nama pemain peringkat 1 kalah
%cytril_duel_top_deat_value_1%  -> jumlah kalah peringkat 1
```
Ganti angka `1` dengan peringkat berapa pun (2, 3, dst — dicache sampai 100 peringkat).

Contoh pemakaian di hologram top 3 kill (mis. lewat DecentHolograms `/dh addline <nama> <baris>`):
```
&6&lTOP KILL CYTRILDUEL
&e#1 %cytril_duel_top_kill_name_1% &7- &a%cytril_duel_top_kill_value_1%
&e#2 %cytril_duel_top_kill_name_2% &7- &a%cytril_duel_top_kill_value_2%
&e#3 %cytril_duel_top_kill_name_3% &7- &a%cytril_duel_top_kill_value_3%
```

Atau di DeluxeMenus untuk membuat GUI leaderboard sendiri, tinggal isi lore item dengan placeholder di atas.

**Leaderboard langsung di chat** (tanpa plugin lain):
```
/duel top win        -> top 10 kemenangan
/duel top kill 20    -> top 20 kill
/duel top deat 5     -> top 5 kekalahan
```
Admin bisa paksa refresh cache dengan `/duel leaderboardreload`.

## 📁 Perintah

| Perintah | Keterangan | Izin |
|---|---|---|
| `/duel` | Buka GUI pilih lawan | `cytrilduel.use` |
| `/duel accept <pemain>` | Terima ajakan duel | `cytrilduel.use` |
| `/duel deny <pemain>` | Tolak ajakan duel | `cytrilduel.use` |
| `/duel cancel` | Batalkan ajakan yang kamu kirim | `cytrilduel.use` |
| `/duel stats [pemain]` | Lihat statistik | `cytrilduel.use` |
| `/duel top <win\|kill\|deat> [jumlah]` | Lihat leaderboard di chat | `cytrilduel.use` |
| `/duel leaderboardreload` | Paksa muat ulang cache leaderboard | `cytrilduel.admin` |
| `/duel setlobby` | Set titik lobby PvP | `cytrilduel.admin` |
| `/duel setarena <nama> <lebar> <kit> <duel\|global>` | Buat/atur arena | `cytrilduel.admin` |
| `/duel setkit <nama>` | Simpan kit dari inventory | `cytrilduel.admin` |
| `/duel spectatorradius <angka>` | Atur batas jarak spectator | `cytrilduel.admin` |
| `/party invite <pemain>` | Undang ke party | `cytrilduel.use` |
| `/party accept <pemain>` | Terima undangan party | `cytrilduel.use` |
| `/party deny <pemain>` | Tolak undangan party | `cytrilduel.use` |
| `/party leave` | Keluar/bubarkan party | `cytrilduel.use` |
| `/party list` | Lihat anggota party | `cytrilduel.use` |

## 🛠️ Cara Setup Awal di Server

1. Pasang plugin (lihat cara mendapatkan file `.jar` di bagian **Build via GitHub** di bawah).
2. Tentukan lobby PvP: berdiri di titik lobby lalu jalankan `/duel setlobby`.
3. Buat kit terlebih dulu: isi inventory sesuai kit yang diinginkan, lalu `/duel setkit kit1` (nama bebas).
4. Buat arena: berdiri di titik tengah arena, lalu:
   ```
   /duel setarena Arena1 20 kit1 duel
   /duel setarena ArenaGlobal 40 kit1,kit2 global
   ```
5. Selesai! Pemain tinggal `/duel` untuk mulai bermain.

## 🚀 Build via GitHub (mendapatkan file .jar)

Karena kompilasi plugin Java butuh akses ke Maven Central & repo PaperMC yang tidak tersedia di lingkungan pembuatan ini, proyek ini sudah dilengkapi **GitHub Actions** yang otomatis mem-build file `.jar` untukmu begitu kode di-push ke GitHub (server GitHub punya akses internet penuh ke semua repository Maven).

Langkah-langkah:

1. **Buat repository baru** di GitHub (bisa privat/publik), misalnya `CytrilDuel`.
2. Upload semua file dari folder ini ke repository tersebut. Bisa lewat web (drag & drop semua file/folder), atau lewat git:
   ```bash
   cd CytrilDuel
   git init
   git add .
   git commit -m "Initial commit CytrilDuel"
   git branch -M main
   git remote add origin https://github.com/USERNAME/CytrilDuel.git
   git push -u origin main
   ```
3. Buka tab **Actions** di repository GitHub-mu. Workflow **"Build CytrilDuel"** akan otomatis berjalan setiap kali kamu push ke branch `main`.
4. Setelah selesai (centang hijau ✅), klik workflow run tersebut → scroll ke bagian **Artifacts** → unduh `CytrilDuel.zip` (isinya file `CytrilDuel.jar`).
5. Masukkan `CytrilDuel.jar` ke folder `plugins/` server Paper/Spigot-mu, lalu restart server.

> Tip: kalau mau, kamu juga bisa membuat **release** (tag versi, misal `v1.0.0`) di GitHub — workflow otomatis akan melampirkan file jar langsung ke halaman Release, jadi tidak perlu buka Artifacts.

## ⚙️ Kebutuhan Server

- Paper/Spigot 1.20.x (Java 17+)
- (Opsional, untuk placeholder) plugin **PlaceholderAPI**

## 📌 Catatan Pengembangan Lanjutan

Beberapa ide fitur tambahan yang bisa dikembangkan lebih lanjut dari base ini (arsitekturnya sudah mendukung untuk diperluas):
- Countdown/hitung mundur sebelum duel dimulai (`duel-countdown` di config sudah disiapkan, tinggal ditambahkan title/actionbar timer di `DuelManager#startDuel`).
- Reset otomatis kondisi arena (blok yang rusak) menggunakan snapshot/WorldEdit API.
- Leaderboard top win/kill (bisa dibaca dari `stats.yml`).
- Efek kill streak & broadcast server-wide untuk streak tertentu.
- Bet/taruhan item atau uang (Vault) sebelum duel dimulai.
- History pertandingan (log siapa vs siapa, arena, waktu, hasil).
