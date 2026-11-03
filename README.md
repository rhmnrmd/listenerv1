# Garry Pedia Listener

Aplikasi Android yang membaca notifikasi dana masuk (DANA, GoPay, OVO, bank, dan lainnya) lalu mengirimnya ke
`POST {server}/api/notify` di gateway. Gateway mencocokkan nominal dengan order yang menunggu, lalu menandainya lunas.

Tampilan memakai token desain yang sama dengan web: ungu `#6D28D9`, kartu bergrid dengan bingkai putus-putus,
judul Bricolage Grotesque, logo "G" dengan titik kuning, dan label status berwarna sama.

## 1. Mendapatkan APK

### Cara A: GitHub (tanpa Android Studio, bisa dari HP)
1. Buat repositori **private** di GitHub, lalu unggah seluruh isi folder ini (termasuk folder `.github`).
2. Di repositori: **Settings > Secrets and variables > Actions**
   - Tab *Secrets*: tambah `NOTIFY_SECRET`, isinya secret notifikasi dari dashboard admin gateway.
   - Tab *Variables* (opsional): tambah `GATEWAY_URL`. Bila kosong dipakai `https://paygarry.paduka.my.id`.
3. Buka tab **Actions > Build APK > Run workflow**. Setelah selesai, unduh artefak `GarryPedia-Listener` (berisi `app-debug.apk`).

### Cara B: Android Studio
1. Salin `gateway.properties.example` menjadi `gateway.properties`, isi `NOTIFY_SECRET`.
2. Buka folder ini di Android Studio (Hedgehog atau lebih baru, JDK 17), tunggu sinkronisasi Gradle.
3. **Build > Build APK(s)**, atau jalankan `gradle assembleDebug`.

Secret yang diisi saat build ikut tertanam di APK. Jangan bagikan APK-nya. Kalau tidak mau menanam secret,
biarkan kosong dan isi dari dalam aplikasi.

## 2. Pengaturan di ponsel (sekali saja)
1. Pasang APK (izinkan pemasangan dari sumber ini).
2. Buka aplikasi, ketuk **Buka izin notifikasi**, lalu aktifkan **Garry Pedia Listener**.
   Android 13+ : bila tombolnya abu-abu, buka **Info aplikasi**, menu titik tiga, **Izinkan setelan terbatas**, lalu ulangi.
3. Ketuk **Atur hemat baterai** dan kecualikan aplikasi ini. Tanpa ini beberapa merek ponsel mematikan listener.
4. Di panel **Server**: alamat server sudah terisi. Isi secret bila belum, tekan **Tes koneksi**, lalu **Simpan**.
5. Kartu ungu menampilkan **Listener aktif** bila semuanya siap.

## 3. Cara kerja
- Notifikasi dari aplikasi yang dicentang dibaca. Yang tidak memuat nominal `Rp...` diabaikan.
- Hanya teks yang memuat salah satu **kata masuk** dan tidak memuat **kata abaikan** yang dikirim
  (transfer keluar, top up, cashback, dan sejenisnya dibuang supaya tidak salah menandai lunas).
- Bila notifikasi punya beberapa baris, dipilih baris yang memuat nominal dan kata masuk, jadi "Saldo Rp..." tidak ikut terbaca.
- Notifikasi yang sama dalam 45 detik hanya dikirim sekali.
- Bila jaringan atau server gagal, pengiriman diulang tiap 15 detik sampai 10 kali, saat internet tersedia.
- Secret salah (401) tidak diulang; riwayat menandainya Gagal.

Status di Riwayat: **Cocok** (order lunas), **Tidak cocok** (tidak ada order menunggu dengan total itu),
**Antre** (menunggu kirim ulang), **Gagal**, **Diabaikan** (aplikasi belum dipilih atau tidak lolos filter).

## 4. Menambah aplikasi merchant
Bila aplikasi penerima QRIS-mu belum ada di daftar, minta pembayaran uji, lalu lihat Riwayat: notifikasinya muncul
sebagai **Diabaikan** lengkap dengan nama paketnya. Ketuk barisnya untuk menambahkan, lalu **Simpan pengaturan**.
Nama paket di daftar bawaan adalah yang umum; cocokkan dengan aplikasi yang kamu pakai.

## 5. Font Inter (opsional)
Judul sudah memakai Bricolage Grotesque (disertakan, lisensi OFL di `LICENSES`). Teks biasa memakai font sistem,
sama seperti cadangan di web. Untuk memakai Inter, taruh `inter.ttf` di `app/src/main/res/font/` sebelum build.

## 6. Catatan keamanan
- Aplikasi hanya mengirim teks dari baris notifikasi yang lolos filter, ke alamat `https://` milikmu. Riwayat hanya menyimpan nominal, nama paket, dan status.
- Jangan commit `gateway.properties`. Berkas ini sudah ada di `.gitignore`.
- Kalau secret pernah bocor, ganti di dashboard admin (Setelan), lalu bangun ulang APK.
