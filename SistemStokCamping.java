import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

// Antarmuka untuk operasi CRUD
interface OperasiCRUDPeralatanCamping {
    void tambahPeralatan(PeralatanCamping peralatan); // Menambahkan data baru
    List<PeralatanCamping> bacaSemuaPeralatan(); // Membaca semua data
    void perbaruiPeralatan(int id, PeralatanCamping peralatan); // Memperbarui data berdasarkan ID
    void hapusPeralatan(int id); // Menghapus data berdasarkan ID
}

// Kelas induk
class Peralatan {
    protected String nama; // Nama peralatan
    protected int stok; // Jumlah stok

    public Peralatan(String nama, int stok) {
        this.nama = nama; // Inisialisasi nama
        this.stok = stok; // Inisialisasi stok
    }

    public String getNama() {
        return nama; // Mengembalikan nama peralatan
    }

    public int getStok() {
        return stok; // Mengembalikan jumlah stok
    }
}

// Kelas turunan
class PeralatanCamping extends Peralatan {
    private Date tanggalRestokTerakhir; // Tanggal terakhir pengisian stok
    private int id; // ID peralatan
    private String namaPenginput; // Nama orang yang menambahkan stok

    public PeralatanCamping(int id, String nama, int stok, Date tanggalRestokTerakhir, String namaPenginput) {
        super(nama, stok); // Memanggil konstruktor dari kelas induk
        this.id = id; // Inisialisasi ID
        this.tanggalRestokTerakhir = tanggalRestokTerakhir; // Inisialisasi tanggal restok
        this.namaPenginput = namaPenginput; // Inisialisasi nama penginput
    }

    public Date getTanggalRestokTerakhir() {
        return tanggalRestokTerakhir; // Mengembalikan tanggal restok terakhir
    }

    public String getTanggalRestokFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy"); // Format tanggal
        return sdf.format(tanggalRestokTerakhir); // Mengembalikan tanggal dalam format string
    }

    public int getId() {
        return id; // Mengembalikan ID
    }

    public String getNamaPenginput() {
        return namaPenginput; // Mengembalikan nama penginput
    }
}

// Implementasi dari antarmuka
class PengelolaPeralatanCamping implements OperasiCRUDPeralatanCamping {
    private Connection koneksi; // Koneksi ke database

    public PengelolaPeralatanCamping() {
        try {
            koneksi = DriverManager.getConnection("jdbc:postgresql://localhost:5432/camping", "postgres", "akashiharuchiyo"); // Koneksi ke database PostgreSQL
        } catch (SQLException e) {
            e.printStackTrace(); // Menangani kesalahan koneksi
        }
    }

    @Override
    public void tambahPeralatan(PeralatanCamping peralatan) {
        String query = "INSERT INTO equipment (id, name, stock, last_restocked, added_by) VALUES (?, ?, ?, ?, ?)"; // Query SQL untuk menambahkan data
        try (PreparedStatement ps = koneksi.prepareStatement(query)) {
            ps.setInt(1, peralatan.getId()); // Mengatur ID
            ps.setString(2, peralatan.getNama()); // Mengatur nama
            ps.setInt(3, peralatan.getStok()); // Mengatur stok
            ps.setDate(4, new java.sql.Date(peralatan.getTanggalRestokTerakhir().getTime())); // Mengatur tanggal restok
            ps.setString(5, peralatan.getNamaPenginput()); // Mengatur nama penginput
            ps.executeUpdate(); // Menjalankan query
        } catch (SQLException e) {
            e.printStackTrace(); // Menangani kesalahan query
        }
    }

    @Override
    public List<PeralatanCamping> bacaSemuaPeralatan() {
        List<PeralatanCamping> daftarPeralatan = new ArrayList<>(); // Membuat daftar untuk menyimpan data
        String query = "SELECT * FROM equipment"; // Query SQL untuk membaca data
        try (Statement stmt = koneksi.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id"); // Membaca ID
                String nama = rs.getString("name"); // Membaca nama
                int stok = rs.getInt("stock"); // Membaca stok
                Date tanggalRestok = rs.getDate("last_restocked"); // Membaca tanggal restok
                String namaPenginput = rs.getString("added_by"); // Membaca nama penginput
                daftarPeralatan.add(new PeralatanCamping(id, nama, stok, tanggalRestok, namaPenginput)); // Menambahkan data ke daftar
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Menangani kesalahan query
        }
        return daftarPeralatan; // Mengembalikan daftar peralatan
    }

    @Override
    public void perbaruiPeralatan(int id, PeralatanCamping peralatan) {
        String query = "UPDATE equipment SET name = ?, stock = ?, last_restocked = ?, added_by = ? WHERE id = ?"; // Query SQL untuk memperbarui data
        try (PreparedStatement ps = koneksi.prepareStatement(query)) {
            ps.setString(1, peralatan.getNama()); // Mengatur nama baru
            ps.setInt(2, peralatan.getStok()); // Mengatur stok baru
            ps.setDate(3, new java.sql.Date(peralatan.getTanggalRestokTerakhir().getTime())); // Mengatur tanggal restok baru
            ps.setString(4, peralatan.getNamaPenginput()); // Mengatur nama penginput baru
            ps.setInt(5, id); // Mengatur ID peralatan yang diperbarui
            ps.executeUpdate(); // Menjalankan query
        } catch (SQLException e) {
            e.printStackTrace(); // Menangani kesalahan query
        }
    }

    @Override
    public void hapusPeralatan(int id) {
        String query = "DELETE FROM equipment WHERE id = ?"; // Query SQL untuk menghapus data
        try (PreparedStatement ps = koneksi.prepareStatement(query)) {
            ps.setInt(1, id); // Mengatur ID peralatan yang akan dihapus
            ps.executeUpdate(); // Menjalankan query
        } catch (SQLException e) {
            e.printStackTrace(); // Menangani kesalahan query
        }
    }
}

public class SistemStokCamping {
    public static void main(String[] args) {
        PengelolaPeralatanCamping pengelola = new PengelolaPeralatanCamping(); // Inisialisasi pengelola
        Scanner scanner = new Scanner(System.in);
        boolean berjalan = true; // Status aplikasi berjalan

        while (berjalan) {
            System.out.println("\nSistem Stok Peralatan Camping");
            System.out.println("1. Tambah Peralatan"); // Tambah data
            System.out.println("2. Lihat Peralatan"); // Lihat data
            System.out.println("3. Perbarui Peralatan"); // Perbarui data
            System.out.println("4. Hapus Peralatan"); // Hapus data
            System.out.println("5. Keluar"); // Keluar
            System.out.print("Pilih opsi: ");
            int pilihan = scanner.nextInt();

            switch (pilihan) {
                case 1: // Menambahkan data baru
                    System.out.print("Masukkan ID peralatan: ");
                    int id = scanner.nextInt(); // Input ID
                    System.out.print("Masukkan nama peralatan: ");
                    String nama = scanner.next(); // Input nama peralatan
                    System.out.print("Masukkan stok: ");
                    int stok = scanner.nextInt(); // Input stok
                    System.out.print("Masukkan tanggal restok terakhir (dd-MM-yyyy): ");
                    String tanggalStr = scanner.next(); // Input tanggal
                    System.out.print("Masukkan nama orang yang menambahkan stok: ");
                    String namaPenginput = scanner.next(); // Input nama penginput stok

                    try {
                        Date tanggal = (Date) new SimpleDateFormat("dd-MM-yyyy").parse(tanggalStr); // Parsing tanggal
                        pengelola.tambahPeralatan(new PeralatanCamping(id, nama, stok, tanggal, namaPenginput)); // Menambah data
                    } catch (Exception e) {
                        e.printStackTrace(); // Menangani kesalahan parsing
                    }
                    break;
                case 2: // Menampilkan data
                    List<PeralatanCamping> daftarPeralatan = pengelola.bacaSemuaPeralatan(); // Membaca data dari database
                    for (PeralatanCamping peralatan : daftarPeralatan) {
                        System.out.printf("ID: %d, Nama: %s, Stok: %d, Tanggal Restok Terakhir: %s, Penginput: %s\n",
                                peralatan.getId(), peralatan.getNama(), peralatan.getStok(), peralatan.getTanggalRestokFormatted(), peralatan.getNamaPenginput()); // Menampilkan data
                    }
                    break;
                case 3: // Memperbarui data
                    System.out.print("Masukkan ID peralatan yang akan diperbarui: ");
                    int idUpdate = scanner.nextInt(); // Input ID
                    System.out.print("Masukkan nama baru: ");
                    String namaUpdate = scanner.next(); // Input nama baru
                    System.out.print("Masukkan stok baru: ");
                    int stokUpdate = scanner.nextInt(); // Input stok baru
                    System.out.print("Masukkan tanggal restok baru (dd-MM-yyyy): ");
                    String tanggalUpdateStr = scanner.next(); // Input tanggal baru
                    System.out.print("Masukkan nama orang yang memperbarui stok: ");
                    String namaPenginputUpdate = scanner.next(); // Input nama penginput baru

                    try {
                        Date tanggalUpdate = new SimpleDateFormat("dd-MM-yyyy").parse(tanggalUpdateStr); // Parsing tanggal baru
                        pengelola.perbaruiPeralatan(idUpdate, new PeralatanCamping(idUpdate, namaUpdate, stokUpdate, tanggalUpdate, namaPenginputUpdate)); // Memperbarui data
                    } catch (Exception e) {
                        e.printStackTrace(); // Menangani kesalahan parsing
                    }
                    break;
                case 4: // Menghapus data
                    System.out.print("Masukkan ID peralatan yang akan dihapus: ");
                    int idDelete = scanner.nextInt(); // Input ID
                    pengelola.hapusPeralatan(idDelete); // Hapus data
                    break;
                case 5: // Keluar dari aplikasi
                    berjalan = false;
                    break;
                default:
                    System.out.println("Pilihan tidak valid.");
                    break;
            }
        }

        scanner.close(); // Menutup scanner
    }
}
