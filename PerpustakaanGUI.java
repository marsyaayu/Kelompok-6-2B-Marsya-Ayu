import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
 * Sistem Manajemen Perpustakaan - Java Swing
 * Penyimpanan data tetap menggunakan ArrayList<String[]> dan file CSV.
 *
 * File CSV yang dipakai:
 * - databuku.csv      : ISBN, Judul, Kategori, Pengarang, Stok
 * - dataanggota.csv   : ID, Nama, No Telp
 * - datapinjam.csv    : ID Pinjam, ID Anggota, ISBN Buku, Tanggal, Status
 *
 * Cara menjalankan:
 * 1. Simpan file ini sebagai PerpustakaanGUI.java
 * 2. Pastikan file CSV berada di folder yang sama dengan file .java / hasil compile
 * 3. Compile: javac PerpustakaanGUI.java
 * 4. Run    : java PerpustakaanGUI
 */
public class PerpustakaanGUI extends JFrame {

    // =========================
    // DATA UTAMA PROGRAM
    // =========================
    static List<String[]> daftarBuku = new ArrayList<>();
    static List<String[]> daftarAnggota = new ArrayList<>();
    static List<String[]> daftarPinjam = new ArrayList<>();

    // Nama file CSV dibuat sama seperti logic console lama.
    static final String FILE_BUKU = "databuku.csv";
    static final String FILE_ANGGOTA = "dataanggota.csv";
    static final String FILE_PINJAM = "datapinjam.csv";

    // Role login. Admin boleh semua fitur, User dibatasi.
    String roleLogin;

    // Komponen tabel dan modelnya.
    JTable tabelBuku, tabelAnggota, tabelPinjam;
    DefaultTableModel modelBuku, modelAnggota, modelPinjam;

    // Field buku.
    JTextField txtIsbn, txtJudul, txtKategori, txtPengarang, txtStok, txtCariBuku;

    // Field anggota.
    JTextField txtIdAnggota, txtNamaAnggota, txtNoTelp, txtCariAnggota;

    // Field peminjaman.
    JTextField txtIdPinjam, txtIdAnggotaPinjam, txtIsbnPinjam, txtCariPinjam;

    // Label dashboard.
    JLabel lblTotalBuku, lblTotalAnggota, lblDipinjam, lblTersedia;

    // Warna tampilan sederhana
    Color warnaUtama = new Color(34, 87, 122);
    Color warnaGelap = new Color(26, 32, 44);
    Color warnaBackground = new Color(245, 247, 250);
    Color warnaPanel = Color.WHITE;
    Color warnaHijau = new Color(46, 125, 50);
    Color warnaMerah = new Color(198, 40, 40);
    Color warnaKuning = new Color(245, 124, 0);
    // warna yang cocok untuk panel tapi tetap ada gradasi biar gak flat banget
    Color warnaPanelGradasi = new Color(245, 247, 250);


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();

            bacaBukuCSV();
            bacaAnggotaCSV();
            bacaPinjamCSV();

            String role = tampilkanLogin();
            if (role != null) {
                new PerpustakaanGUI(role).setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }

    // Menggunakan look and feel bawaan sistem
    static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Gagal mengatur tampilan bawaan sistem.");
        }
    }

    // =========================
    // LOGIN ROLE
    // =========================
    static String tampilkanLogin() {
        final String[] roleTerpilih = { null };

        JDialog dialog = new JDialog((Frame) null, "Login Perpustakaan", true);
        dialog.setSize(520, 420);
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(240, 244, 248));
        background.setBorder(new EmptyBorder(28, 28, 28, 28));

        JPanel container = new JPanel(new BorderLayout(0, 24));
        container.setOpaque(false);

        JLabel judul = new JLabel("System Perpustakaan", SwingConstants.CENTER);
        judul.setFont(new Font("Segoe UI", Font.BOLD, 34));
        judul.setForeground(new Color(26, 32, 44));

        RoundedPanel kartuLogin = new RoundedPanel(30, Color.WHITE);
        kartuLogin.setLayout(new GridBagLayout());
        kartuLogin.setBorder(new EmptyBorder(32, 42, 36, 42));
        kartuLogin.setPreferredSize(new Dimension(340, 220));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 0);

        JLabel pilih = new JLabel("Pilih", SwingConstants.CENTER);
        pilih.setFont(new Font("Segoe UI", Font.BOLD, 36));
        pilih.setForeground(new Color(45, 55, 72));
        kartuLogin.add(pilih, gbc);

        gbc.gridy = 1;
        JButton btnAdmin = tombolLogin("Admin", new Color(33, 150, 243));
        kartuLogin.add(btnAdmin, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        JButton btnUser = tombolLogin("User", new Color(46, 125, 50));
        kartuLogin.add(btnUser, gbc);

        // Action listener untuk tombol login
        btnAdmin.addActionListener(e -> {
            roleTerpilih[0] = "Admin";
            dialog.dispose();
        });

        // Action listener untuk tombol login
        btnUser.addActionListener(e -> {
            roleTerpilih[0] = "User";
            dialog.dispose();
        });

        // Menambahkan komponen ke dialog
        container.add(judul, BorderLayout.NORTH);
        container.add(kartuLogin, BorderLayout.CENTER);

        background.add(container);
        dialog.setContentPane(background);
        dialog.setVisible(true);

        return roleTerpilih[0];
    }

    static JButton tombolLogin(String teks, Color warna) {
        JButton tombol = new JButton(teks);
        tombol.setFocusPainted(false);
        tombol.setBackground(warna);
        tombol.setForeground(Color.black);
        tombol.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tombol.setPreferredSize(new Dimension(220, 44));
        tombol.setBorder(new EmptyBorder(10, 18, 10, 18));
        tombol.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return tombol;
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private final Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillRoundRect(6, 8, getWidth() - 12, getHeight() - 12, radius, radius);

            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth() - 12, getHeight() - 12, radius, radius);

            g2.dispose();
        }
    }

    public PerpustakaanGUI(String roleLogin) {
        this.roleLogin = roleLogin;
        ImageIcon icon = new ImageIcon("logo.png");
        setIconImage(icon.getImage());
        setTitle("Sistem Manajemen Perpustakaan - Login sebagai " + roleLogin);
        setSize(1100, 680);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 600));

        initKomponenUtama();
        refreshSemuaTabel();
        updateDashboard();
    }

    // =========================
    // LAYOUT UTAMA
    // =========================
    void initKomponenUtama() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(warnaBackground);
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JPanel header = buatHeader();
        root.add(header, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.addTab("Dashboard", buatPanelDashboard());
        tabbedPane.addTab("Data Buku", buatPanelBuku());

        if (roleLogin.equals("Admin")) {
            tabbedPane.addTab("Data Anggota", buatPanelAnggota());
        }

        tabbedPane.addTab("Peminjaman", buatPanelPeminjaman());

        root.add(tabbedPane, BorderLayout.CENTER);
    }

    JPanel buatHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(warnaUtama);
        panel.setBorder(new EmptyBorder(14, 18, 14, 18));

        JLabel judul = new JLabel("Sistem Manajemen Perpustakaan");
        judul.setForeground(Color.WHITE);
        judul.setFont(new Font("Segoe UI", Font.BOLD, 24));

        JLabel role = new JLabel("Role: " + roleLogin);
        role.setForeground(Color.WHITE);
        role.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        JButton btnLogout = new JButton("Ganti Role");
        btnLogout.addActionListener(e -> gantiRole());

        panel.add(judul, BorderLayout.WEST);
        panel.add(role, BorderLayout.CENTER);
        panel.add(btnLogout, BorderLayout.EAST);
        return panel;
    }

    void gantiRole() {
        String role = tampilkanLogin();
        if (role != null) {
            dispose();
            new PerpustakaanGUI(role).setVisible(true);
        }
    }

    // =========================
    // DASHBOARD
    // =========================
    JPanel buatPanelDashboard() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 16, 16));
        panel.setBackground(warnaBackground);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));

        lblTotalBuku = new JLabel("0");
        lblTotalAnggota = new JLabel("0");
        lblDipinjam = new JLabel("0");
        lblTersedia = new JLabel("0");

        panel.add(buatKartuDashboard("Total Judul Buku", lblTotalBuku));
        panel.add(buatKartuDashboard("Total Anggota", lblTotalAnggota));
        panel.add(buatKartuDashboard("Buku Sedang Dipinjam", lblDipinjam));
        panel.add(buatKartuDashboard("Total Stok Tersedia", lblTersedia));

        return panel;
    }

    JPanel buatKartuDashboard(String teks, JLabel angka) {
        JPanel kartu = new JPanel(new BorderLayout());
        kartu.setBackground(warnaPanel);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 230, 235)),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel label = new JLabel(teks);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(warnaGelap);

        angka.setFont(new Font("Segoe UI", Font.BOLD, 42));
        angka.setForeground(warnaUtama);

        kartu.add(label, BorderLayout.NORTH);
        kartu.add(angka, BorderLayout.CENTER);
        return kartu;
    }

    void updateDashboard() {
        int totalBuku = daftarBuku.size();
        int totalAnggota = daftarAnggota.size();
        int sedangDipinjam = 0;
        int bukuTersedia = 0;

        for (String[] p : daftarPinjam) {
            if (p.length >= 5 && p[4].equalsIgnoreCase("Dipinjam")) {
                sedangDipinjam++;
            }
        }

        for (String[] b : daftarBuku) {
            if (b.length >= 5 && isAngka(b[4])) {
                bukuTersedia += Integer.parseInt(b[4]);
            }
        }

        lblTotalBuku.setText(String.valueOf(totalBuku));
        lblTotalAnggota.setText(String.valueOf(totalAnggota));
        lblDipinjam.setText(String.valueOf(sedangDipinjam));
        lblTersedia.setText(String.valueOf(bukuTersedia));
    }

    // =========================
    // PANEL BUKU
    // =========================
    JPanel buatPanelBuku() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(warnaBackground);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        modelBuku = new DefaultTableModel(new String[] { "ISBN", "Judul", "Kategori", "Pengarang", "Stok" }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelBuku = new JTable(modelBuku);
        aturTampilanTabel(tabelBuku);
        tabelBuku.getSelectionModel().addListSelectionListener(e -> isiFieldBukuDariTabel());

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBackground(warnaPanel);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Form Buku"),
                new EmptyBorder(8, 8, 8, 8)));

        txtIsbn = new JTextField();
        txtJudul = new JTextField();
        txtKategori = new JTextField();
        txtPengarang = new JTextField();
        txtStok = new JTextField();
        txtCariBuku = new JTextField();
        JComboBox<String> cbSortBuku = new JComboBox<>(new String[] {
                "Sort: Default",
                "ISBN A-Z",
                "Judul A-Z",
                "Kategori A-Z",
                "Pengarang A-Z",
                "Stok Terbanyak",
                "Stok Tersedikit"
        });

        form.add(new JLabel("ISBN"));
        form.add(txtIsbn);
        form.add(new JLabel("Judul"));
        form.add(txtJudul);
        form.add(new JLabel("Kategori"));
        form.add(txtKategori);
        form.add(new JLabel("Pengarang"));
        form.add(txtPengarang);
        form.add(new JLabel("Stok"));
        form.add(txtStok);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tombol.setBackground(warnaPanel);
        JButton btnTambah = tombol("Tambah", warnaHijau);
        JButton btnEdit = tombol("Edit", warnaKuning);
        JButton btnHapus = tombol("Hapus", warnaMerah);
        JButton btnCari = tombol("Search", warnaUtama);
        JButton btnRefresh = tombol("Refresh", warnaUtama);
        JButton btnBersih = tombol("Clear", warnaGelap);

        // action
        btnTambah.addActionListener(e -> tambahBukuGUI());
        btnEdit.addActionListener(e -> editBukuGUI());
        btnHapus.addActionListener(e -> hapusBukuGUI());
        btnCari.addActionListener(e -> cariBukuGUI());
        btnRefresh.addActionListener(e -> refreshBuku());
        btnBersih.addActionListener(e -> bersihkanFieldBuku());
        cbSortBuku.addActionListener(e -> {
            cariBukuGUI();
            sortTabelBuku(cbSortBuku.getSelectedItem().toString());
        });

        tombol.add(btnTambah);
        tombol.add(btnEdit);
        tombol.add(btnHapus);
        tombol.add(btnCari);
        tombol.add(btnRefresh);
        tombol.add(btnBersih);

        JPanel kiri = new JPanel(new BorderLayout(10, 10));
        kiri.setBackground(warnaBackground);
        kiri.add(form, BorderLayout.NORTH);
        kiri.add(tombol, BorderLayout.CENTER);
        kiri.setPreferredSize(new Dimension(330, 0));

        if (roleLogin.equals("User")) {
            btnTambah.setEnabled(false);
            btnEdit.setEnabled(false);
            btnHapus.setEnabled(false);
            txtIsbn.setEditable(false);
            txtJudul.setEditable(false);
            txtKategori.setEditable(false);
            txtPengarang.setEditable(false);
            txtStok.setEditable(false);
        }

        JPanel kanan = new JPanel(new BorderLayout(8, 8));
        kanan.setBackground(warnaBackground);

        JPanel panelCari = new JPanel(new BorderLayout(8, 8));
        panelCari.setBackground(warnaBackground);

        txtCariBuku = new JTextField();
        JButton btnCariAtas = tombol("Search", warnaUtama);
        JButton btnRefreshAtas = tombol("Refresh", warnaUtama);

        JPanel tombolCari = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombolCari.setBackground(warnaBackground);
        tombolCari.add(cbSortBuku);
        tombolCari.add(btnCariAtas);
        tombolCari.add(btnRefreshAtas);

        panelCari.add(new JLabel("Cari Buku:"), BorderLayout.WEST);
        panelCari.add(txtCariBuku, BorderLayout.CENTER);
        panelCari.add(tombolCari, BorderLayout.EAST);

        btnCariAtas.addActionListener(e -> {
            cariBukuGUI();
            sortTabelBuku(cbSortBuku.getSelectedItem().toString());
        });
        btnRefreshAtas.addActionListener(e -> refreshBuku());

        kanan.add(panelCari, BorderLayout.NORTH);
        kanan.add(new JScrollPane(tabelBuku), BorderLayout.CENTER);

        if (roleLogin.equals("Admin")) {
            panel.add(kiri, BorderLayout.WEST);
        }

        panel.add(kanan, BorderLayout.CENTER);
        return panel;
    }

    void tambahBukuGUI() {
        String isbn = txtIsbn.getText().trim();
        String judul = txtJudul.getText().trim();
        String kategori = txtKategori.getText().trim();
        String pengarang = txtPengarang.getText().trim();
        String stok = txtStok.getText().trim();

        if (adaKosong(isbn, judul, kategori, pengarang, stok)) {
            pesanError("Semua field buku wajib diisi.");
            return;
        }
        if (mengandungKoma(isbn, judul, kategori, pengarang, stok)) {
            pesanError("Input tidak boleh mengandung koma (,), karena CSV bisa berantakan. Tragis tapi nyata.");
            return;
        }
        if (!isAngka(stok)) {
            pesanError("Stok harus angka.");
            return;
        }
        if (cariIndexBukuByISBN(isbn) != -1) {
            pesanError("ISBN sudah ada.");
            return;
        }

        daftarBuku.add(new String[] { isbn, judul, kategori, pengarang, stok });
        simpanBukuCSV();
        refreshSemuaTabel();
        bersihkanFieldBuku();
        pesanInfo("Buku berhasil ditambahkan.");
    }

    void editBukuGUI() {
        String isbn = txtIsbn.getText().trim();
        int index = cariIndexBukuByISBN(isbn);
        if (index == -1) {
            pesanError("Pilih buku atau masukkan ISBN yang valid.");
            return;
        }

        String judul = txtJudul.getText().trim();
        String kategori = txtKategori.getText().trim();
        String pengarang = txtPengarang.getText().trim();
        String stok = txtStok.getText().trim();

        if (adaKosong(judul, kategori, pengarang, stok)) {
            pesanError("Data buku tidak boleh kosong.");
            return;
        }
        if (mengandungKoma(isbn, judul, kategori, pengarang, stok)) {
            pesanError("Input tidak boleh mengandung koma (,).");
            return;
        }
        if (!isAngka(stok)) {
            pesanError("Stok harus angka.");
            return;
        }

        daftarBuku.set(index, new String[] { isbn, judul, kategori, pengarang, stok });
        simpanBukuCSV();
        refreshSemuaTabel();
        pesanInfo("Buku berhasil diedit.");
    }

    void hapusBukuGUI() {
        String isbn = txtIsbn.getText().trim();
        int index = cariIndexBukuByISBN(isbn);
        if (index == -1) {
            pesanError("Pilih buku yang ingin dihapus.");
            return;
        }

        for (String[] p : daftarPinjam) {
            if (p[2].equals(isbn) && p[4].equalsIgnoreCase("Dipinjam")) {
                pesanError("Buku sedang dipinjam, jadi tidak bisa dihapus.");
                return;
            }
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin hapus buku ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            daftarBuku.remove(index);
            simpanBukuCSV();
            refreshSemuaTabel();
            bersihkanFieldBuku();
            pesanInfo("Buku berhasil dihapus.");
        }
    }

    void cariBukuGUI() {
        String keyword = txtCariBuku.getText().trim().toLowerCase();
        modelBuku.setRowCount(0);

        for (String[] b : daftarBuku) {
            if (b[0].toLowerCase().contains(keyword)
                    || b[1].toLowerCase().contains(keyword)
                    || b[2].toLowerCase().contains(keyword)
                    || b[3].toLowerCase().contains(keyword)
                    || b[4].toLowerCase().contains(keyword)) {
                modelBuku.addRow(b);
            }
        }
    }

    void sortTabelBuku(String tipeSort) {
        List<String[]> data = new ArrayList<>();

        for (int i = 0; i < modelBuku.getRowCount(); i++) {
            data.add(new String[] {
                    modelBuku.getValueAt(i, 0).toString(),
                    modelBuku.getValueAt(i, 1).toString(),
                    modelBuku.getValueAt(i, 2).toString(),
                    modelBuku.getValueAt(i, 3).toString(),
                    modelBuku.getValueAt(i, 4).toString()
            });
        }

        switch (tipeSort) {
            case "ISBN A-Z":
                data.sort((a, b) -> a[0].compareToIgnoreCase(b[0]));
                break;
            case "Judul A-Z":
                data.sort((a, b) -> a[1].compareToIgnoreCase(b[1]));
                break;
            case "Kategori A-Z":
                data.sort((a, b) -> a[2].compareToIgnoreCase(b[2]));
                break;
            case "Pengarang A-Z":
                data.sort((a, b) -> a[3].compareToIgnoreCase(b[3]));
                break;
            case "Stok Terbanyak":
                data.sort((a, b) -> Integer.parseInt(b[4]) - Integer.parseInt(a[4]));
                break;
            case "Stok Tersedikit":
                data.sort((a, b) -> Integer.parseInt(a[4]) - Integer.parseInt(b[4]));
                break;
            default:
                return;
        }

        modelBuku.setRowCount(0);
        for (String[] b : data) {
            modelBuku.addRow(b);
        }
    }

    void refreshBuku() {
        txtCariBuku.setText("");
        refreshTabelBuku();
    }

    void isiFieldBukuDariTabel() {
        int row = tabelBuku.getSelectedRow();
        if (row >= 0) {
            txtIsbn.setText(modelBuku.getValueAt(row, 0).toString());
            txtJudul.setText(modelBuku.getValueAt(row, 1).toString());
            txtKategori.setText(modelBuku.getValueAt(row, 2).toString());
            txtPengarang.setText(modelBuku.getValueAt(row, 3).toString());
            txtStok.setText(modelBuku.getValueAt(row, 4).toString());
        }
    }

    void bersihkanFieldBuku() {
        txtIsbn.setText("");
        txtJudul.setText("");
        txtKategori.setText("");
        txtPengarang.setText("");
        txtStok.setText("");
        txtCariBuku.setText("");
        tabelBuku.clearSelection();
    }

    // =========================
    // PANEL ANGGOTA
    // =========================
    JPanel buatPanelAnggota() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(warnaBackground);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        modelAnggota = new DefaultTableModel(new String[] { "ID", "Nama", "No Telp" }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelAnggota = new JTable(modelAnggota);
        aturTampilanTabel(tabelAnggota);
        tabelAnggota.getSelectionModel().addListSelectionListener(e -> isiFieldAnggotaDariTabel());

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setBackground(warnaPanel);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Form Anggota"),
                new EmptyBorder(8, 8, 8, 8)));

        txtIdAnggota = new JTextField();
        txtIdAnggota.setEditable(false);
        txtNamaAnggota = new JTextField();
        txtNoTelp = new JTextField();
        txtCariAnggota = new JTextField();
        JComboBox<String> cbSortAnggota = new JComboBox<>(new String[] {
                "Sort: Default",
                "ID A-Z",
                "Nama A-Z",
                "No Telp A-Z"
        });

        form.add(new JLabel("ID Otomatis"));
        form.add(txtIdAnggota);
        form.add(new JLabel("Nama"));
        form.add(txtNamaAnggota);
        form.add(new JLabel("No Telp"));
        form.add(txtNoTelp);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tombol.setBackground(warnaPanel);
        JButton btnTambah = tombol("Tambah", warnaHijau);
        JButton btnEdit = tombol("Edit", warnaKuning);
        JButton btnHapus = tombol("Hapus", warnaMerah);
        JButton btnCari = tombol("Search", warnaUtama);
        JButton btnRefresh = tombol("Refresh", warnaUtama);
        JButton btnBersih = tombol("Clear", warnaGelap);

        btnTambah.addActionListener(e -> tambahAnggotaGUI());
        btnEdit.addActionListener(e -> editAnggotaGUI());
        btnHapus.addActionListener(e -> hapusAnggotaGUI());
        btnCari.addActionListener(e -> cariAnggotaGUI());
        btnRefresh.addActionListener(e -> refreshAnggota());
        btnBersih.addActionListener(e -> bersihkanFieldAnggota());

        tombol.add(btnTambah);
        tombol.add(btnEdit);
        tombol.add(btnHapus);
        tombol.add(btnBersih);

        JPanel kiri = new JPanel(new BorderLayout(10, 10));
        kiri.setBackground(warnaBackground);
        kiri.add(form, BorderLayout.NORTH);
        kiri.add(tombol, BorderLayout.CENTER);
        kiri.setPreferredSize(new Dimension(330, 0));

        if (roleLogin.equals("User")) {
            btnTambah.setEnabled(false);
            btnEdit.setEnabled(false);
            btnHapus.setEnabled(false);
            btnCari.setEnabled(false);
            txtNamaAnggota.setEditable(false);
            txtNoTelp.setEditable(false);
            txtCariAnggota.setEditable(false);
        }

        JPanel kanan = new JPanel(new BorderLayout(8, 8));
        kanan.setBackground(warnaBackground);

        JPanel panelCari = new JPanel(new BorderLayout(8, 8));
        panelCari.setBackground(warnaBackground);

        txtCariAnggota = new JTextField();

        JButton btnCariAtas = tombol("Search", warnaUtama);
        JButton btnRefreshAtas = tombol("Refresh", warnaUtama);

        JPanel tombolCari = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tombolCari.setBackground(warnaBackground);
        tombolCari.add(cbSortAnggota);
        tombolCari.add(btnCariAtas);
        tombolCari.add(btnRefreshAtas);

        panelCari.add(new JLabel("Cari Anggota:"), BorderLayout.WEST);
        panelCari.add(txtCariAnggota, BorderLayout.CENTER);
        panelCari.add(tombolCari, BorderLayout.EAST);

        btnCariAtas.addActionListener(e -> {
            cariAnggotaGUI();
            sortTabelAnggota(cbSortAnggota.getSelectedItem().toString());
        });
        btnRefreshAtas.addActionListener(e -> refreshAnggota());

        kanan.add(panelCari, BorderLayout.NORTH);
        kanan.add(new JScrollPane(tabelAnggota), BorderLayout.CENTER);

        panel.add(kiri, BorderLayout.WEST);
        panel.add(kanan, BorderLayout.CENTER);
        return panel;
    }

    void sortTabelAnggota(String tipeSort) {
        List<String[]> data = new ArrayList<>();

        for (int i = 0; i < modelAnggota.getRowCount(); i++) {
            data.add(new String[] {
                    modelAnggota.getValueAt(i, 0).toString(),
                    modelAnggota.getValueAt(i, 1).toString(),
                    modelAnggota.getValueAt(i, 2).toString()
            });
        }

        switch (tipeSort) {
            case "ID A-Z":
                data.sort((a, b) -> a[0].compareToIgnoreCase(b[0]));
                break;
            case "Nama A-Z":
                data.sort((a, b) -> a[1].compareToIgnoreCase(b[1]));
                break;
            case "No Telp A-Z":
                data.sort((a, b) -> a[2].compareToIgnoreCase(b[2]));
                break;
            default:
                return;
        }

        modelAnggota.setRowCount(0);
        for (String[] a : data) {
            modelAnggota.addRow(a);
        }
    }

    void tambahAnggotaGUI() {
        String nama = txtNamaAnggota.getText().trim();
        String noTelp = txtNoTelp.getText().trim();

        if (adaKosong(nama, noTelp)) {
            pesanError("Nama dan No Telp wajib diisi.");
            return;
        }
        if (mengandungKoma(nama, noTelp)) {
            pesanError("Input tidak boleh mengandung koma (,).");
            return;
        }

        String idBaru = generateIdAnggota();
        daftarAnggota.add(new String[] { idBaru, nama, noTelp });
        simpanAnggotaCSV();
        refreshSemuaTabel();
        bersihkanFieldAnggota();
        pesanInfo("Anggota berhasil ditambahkan dengan ID: " + idBaru);
    }

    void editAnggotaGUI() {
        String id = txtIdAnggota.getText().trim();
        int index = cariIndexAnggotaByID(id);
        if (index == -1) {
            pesanError("Pilih anggota yang ingin diedit.");
            return;
        }

        String nama = txtNamaAnggota.getText().trim();
        String noTelp = txtNoTelp.getText().trim();

        if (adaKosong(nama, noTelp)) {
            pesanError("Nama dan No Telp tidak boleh kosong.");
            return;
        }
        if (mengandungKoma(nama, noTelp)) {
            pesanError("Input tidak boleh mengandung koma (,).");
            return;
        }

        daftarAnggota.set(index, new String[] { id, nama, noTelp });
        simpanAnggotaCSV();
        refreshSemuaTabel();
        pesanInfo("Anggota berhasil diedit.");
    }

    void hapusAnggotaGUI() {
        String id = txtIdAnggota.getText().trim();
        int index = cariIndexAnggotaByID(id);
        if (index == -1) {
            pesanError("Pilih anggota yang ingin dihapus.");
            return;
        }

        for (String[] p : daftarPinjam) {
            if (p[1].equals(id) && p[4].equalsIgnoreCase("Dipinjam")) {
                pesanError("Anggota masih punya pinjaman aktif.");
                return;
            }
        }

        int konfirmasi = JOptionPane.showConfirmDialog(this, "Yakin hapus anggota ini?", "Konfirmasi",
                JOptionPane.YES_NO_OPTION);
        if (konfirmasi == JOptionPane.YES_OPTION) {
            daftarAnggota.remove(index);
            simpanAnggotaCSV();
            refreshSemuaTabel();
            bersihkanFieldAnggota();
            pesanInfo("Anggota berhasil dihapus.");
        }
    }

    void cariAnggotaGUI() {
        String keyword = txtCariAnggota.getText().trim().toLowerCase();
        modelAnggota.setRowCount(0);

        for (String[] a : daftarAnggota) {
            if (a[0].toLowerCase().contains(keyword)
                    || a[1].toLowerCase().contains(keyword)
                    || a[2].toLowerCase().contains(keyword)) {
                modelAnggota.addRow(a);
            }
        }
    }

    void refreshAnggota() {
        txtCariAnggota.setText("");
        refreshTabelAnggota();
    }

    void isiFieldAnggotaDariTabel() {
        int row = tabelAnggota.getSelectedRow();
        if (row >= 0) {
            txtIdAnggota.setText(modelAnggota.getValueAt(row, 0).toString());
            txtNamaAnggota.setText(modelAnggota.getValueAt(row, 1).toString());
            txtNoTelp.setText(modelAnggota.getValueAt(row, 2).toString());
        }
    }

    void bersihkanFieldAnggota() {
        txtIdAnggota.setText("");
        txtNamaAnggota.setText("");
        txtNoTelp.setText("");
        txtCariAnggota.setText("");
        tabelAnggota.clearSelection();
    }

    String generateIdAnggota() {
        int max = 0;
        for (String[] a : daftarAnggota) {
            try {
                if (a[0].startsWith("A")) {
                    int nomor = Integer.parseInt(a[0].substring(1));
                    if (nomor > max)
                        max = nomor;
                }
            } catch (Exception ignored) {
                // Kalau ada ID rusak, dilewati saja. Hidup sudah cukup rusak.
            }
        }
        return "A" + String.format("%03d", max + 1);
    }

    // =========================
    // PANEL PEMINJAMAN
    // =========================
    JPanel buatPanelPeminjaman() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(warnaBackground);
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        modelPinjam = new DefaultTableModel(
                new String[] { "ID Pinjam", "ID Anggota", "ISBN Buku", "Tanggal", "Status" }, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelPinjam = new JTable(modelPinjam);
        aturTampilanTabel(tabelPinjam);
        tabelPinjam.getSelectionModel().addListSelectionListener(e -> isiFieldPinjamDariTabel());

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBackground(warnaPanel);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Form Peminjaman"),
                new EmptyBorder(8, 8, 8, 8)));

        txtIdAnggotaPinjam = new JTextField();
        txtIsbnPinjam = new JTextField();
        txtCariPinjam = new JTextField();

        form.add(new JLabel("ID Anggota"));
        form.add(txtIdAnggotaPinjam);
        form.add(new JLabel("ISBN Buku"));
        form.add(txtIsbnPinjam);

        JPanel tombol = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tombol.setBackground(warnaPanel);
        JButton btnPinjam = tombol("Pinjam Buku", warnaHijau);
        JButton btnBersih = tombol("Clear", warnaGelap);

        btnPinjam.addActionListener(e -> pinjamBukuGUI());
        btnBersih.addActionListener(e -> bersihkanFieldPinjam());

        tombol.add(btnPinjam);
        tombol.add(btnBersih);

        JPanel kiri = new JPanel(new BorderLayout(10, 10));
        kiri.setBackground(warnaBackground);
        kiri.add(form, BorderLayout.NORTH);
        kiri.add(tombol, BorderLayout.CENTER);
        kiri.setPreferredSize(new Dimension(330, 0));

        JPanel kanan = new JPanel(new BorderLayout(8, 8));
        kanan.setBackground(warnaBackground);

        JPanel panelAtas = new JPanel(new BorderLayout(8, 8));
        panelAtas.setBackground(warnaBackground);

        txtCariPinjam = new JTextField();

        JButton btnCariAtas = tombol("Search", warnaUtama);
        JButton btnRefreshAtas = tombol("Refresh", warnaUtama);
        JButton btnKembaliAtas = tombol("Kembalikan", warnaKuning);

        JPanel tombolAtas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        tombolAtas.setBackground(warnaBackground);
        tombolAtas.add(btnCariAtas);
        tombolAtas.add(btnRefreshAtas);
        tombolAtas.add(btnKembaliAtas);

        panelAtas.add(new JLabel("Cari Peminjaman:"), BorderLayout.WEST);
        panelAtas.add(txtCariPinjam, BorderLayout.CENTER);
        panelAtas.add(tombolAtas, BorderLayout.EAST);

        btnCariAtas.addActionListener(e -> cariPinjamGUI());
        btnRefreshAtas.addActionListener(e -> refreshPinjam());
        btnKembaliAtas.addActionListener(e -> kembalikanBukuGUI());

        kanan.add(panelAtas, BorderLayout.NORTH);
        kanan.add(new JScrollPane(tabelPinjam), BorderLayout.CENTER);

        panel.add(kiri, BorderLayout.WEST);
        panel.add(kanan, BorderLayout.CENTER);
        return panel;
    }

    void pinjamBukuGUI() {
        String idAnggota = txtIdAnggotaPinjam.getText().trim();
        String isbn = txtIsbnPinjam.getText().trim();

        if (adaKosong(idAnggota, isbn)) {
            pesanError("ID Anggota dan ISBN Buku wajib diisi.");
            return;
        }

        int indexAnggota = cariIndexAnggotaByID(idAnggota);
        if (indexAnggota == -1) {
            pesanError("ID Anggota tidak ditemukan.");
            return;
        }

        int indexBuku = cariIndexBukuByISBN(isbn);
        if (indexBuku == -1) {
            pesanError("ISBN Buku tidak ditemukan.");
            return;
        }

        String[] buku = daftarBuku.get(indexBuku);
        int stok = Integer.parseInt(buku[4]);
        if (stok <= 0) {
            pesanError("Stok buku habis.");
            return;
        }

        String idPinjam = generateIdPinjam();
        String tanggal = LocalDate.now().toString();

        daftarPinjam.add(new String[] { idPinjam, idAnggota, isbn, tanggal, "Dipinjam" });
        buku[4] = String.valueOf(stok - 1);

        simpanBukuCSV();
        simpanPinjamCSV();
        refreshSemuaTabel();
        bersihkanFieldPinjam();
        pesanInfo("Peminjaman berhasil. ID Pinjam: " + idPinjam);
    }

    void kembalikanBukuGUI() {
        int row = tabelPinjam.getSelectedRow();

        if (row < 0) {
            pesanError("Pilih data peminjaman di tabel dulu.");
            return;
        }

        String idPinjam = modelPinjam.getValueAt(row, 0).toString();

        int indexPinjam = cariIndexPinjamByID(idPinjam);
        if (indexPinjam == -1) {
            pesanError("ID Pinjam tidak ditemukan.");
            return;
        }

        String[] pinjam = daftarPinjam.get(indexPinjam);
        if (pinjam[4].equalsIgnoreCase("Dikembalikan")) {
            pesanError("Buku ini sudah dikembalikan sebelumnya.");
            return;
        }

        pinjam[4] = "Dikembalikan";
        int indexBuku = cariIndexBukuByISBN(pinjam[2]);
        if (indexBuku != -1) {
            String[] buku = daftarBuku.get(indexBuku);
            buku[4] = String.valueOf(Integer.parseInt(buku[4]) + 1);
        }

        simpanBukuCSV();
        simpanPinjamCSV();
        refreshSemuaTabel();
        pesanInfo("Buku berhasil dikembalikan.");
    }

    void cariPinjamGUI() {
        String keyword = txtCariPinjam.getText().trim().toLowerCase();
        modelPinjam.setRowCount(0);

        for (String[] p : daftarPinjam) {
            if (p[0].toLowerCase().contains(keyword)
                    || p[1].toLowerCase().contains(keyword)
                    || p[2].toLowerCase().contains(keyword)
                    || p[3].toLowerCase().contains(keyword)
                    || p[4].toLowerCase().contains(keyword)) {
                modelPinjam.addRow(p);
            }
        }
    }

    void riwayatAnggotaGUI() {
        String id = txtIdAnggotaPinjam.getText().trim();
        if (id.isEmpty()) {
            id = JOptionPane.showInputDialog(this, "Masukkan ID Anggota:");
            if (id == null || id.trim().isEmpty())
                return;
            id = id.trim();
        }

        modelPinjam.setRowCount(0);
        boolean ketemu = false;
        for (String[] p : daftarPinjam) {
            if (p[1].equalsIgnoreCase(id)) {
                modelPinjam.addRow(p);
                ketemu = true;
            }
        }

        if (!ketemu) {
            pesanInfo("Belum ada riwayat peminjaman untuk anggota: " + id);
            refreshTabelPinjam();
        }
    }

    void refreshPinjam() {
        txtCariPinjam.setText("");
        refreshTabelPinjam();
    }

    void isiFieldPinjamDariTabel() {
        int row = tabelPinjam.getSelectedRow();

        if (row >= 0) {
            txtIdAnggotaPinjam.setText(modelPinjam.getValueAt(row, 1).toString());
            txtIsbnPinjam.setText(modelPinjam.getValueAt(row, 2).toString());
        }
    }

    void bersihkanFieldPinjam() {
        txtIdAnggotaPinjam.setText("");
        txtIsbnPinjam.setText("");

        if (txtCariPinjam != null) {
            txtCariPinjam.setText("");
        }

        if (tabelPinjam != null) {
            tabelPinjam.clearSelection();
        }
    }

    String generateIdPinjam() {
        int max = 0;
        for (String[] p : daftarPinjam) {
            try {
                if (p[0].startsWith("P")) {
                    int nomor = Integer.parseInt(p[0].substring(1));
                    if (nomor > max)
                        max = nomor;
                }
            } catch (Exception ignored) {
                // ID rusak dilewati.
            }
        }
        return "P" + String.format("%03d", max + 1);
    }

    // =========================
    // REFRESH TABEL
    // =========================
    void refreshSemuaTabel() {
        if (modelBuku != null) {
            refreshTabelBuku();
        }

        if (modelAnggota != null) {
            refreshTabelAnggota();
        }

        if (modelPinjam != null) {
            refreshTabelPinjam();
        }

        updateDashboard();
    }

    void refreshTabelBuku() {
        modelBuku.setRowCount(0);
        for (String[] b : daftarBuku) {
            modelBuku.addRow(b);
        }
    }

    void refreshTabelAnggota() {
        if (modelAnggota == null)
            return;

        modelAnggota.setRowCount(0);
        for (String[] a : daftarAnggota) {
            modelAnggota.addRow(a);
        }
    }

    void refreshTabelPinjam() {
        if (modelPinjam == null)
            return;

        modelPinjam.setRowCount(0);
        for (String[] p : daftarPinjam) {
            modelPinjam.addRow(p);
        }
    }

    // =========================
    // PENCARIAN INDEX DATA
    // =========================
    int cariIndexBukuByISBN(String isbn) {
        for (int i = 0; i < daftarBuku.size(); i++) {
            if (daftarBuku.get(i)[0].equalsIgnoreCase(isbn)) {
                return i;
            }
        }
        return -1;
    }

    int cariIndexAnggotaByID(String id) {
        for (int i = 0; i < daftarAnggota.size(); i++) {
            if (daftarAnggota.get(i)[0].equalsIgnoreCase(id)) {
                return i;
            }
        }
        return -1;
    }

    int cariIndexPinjamByID(String idPinjam) {
        for (int i = 0; i < daftarPinjam.size(); i++) {
            if (daftarPinjam.get(i)[0].equalsIgnoreCase(idPinjam)) {
                return i;
            }
        }
        return -1;
    }

    // =========================
    // BACA CSV
    // =========================
    static void bacaBukuCSV() {
        daftarBuku.clear();
        File file = new File(FILE_BUKU);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length == 5) {
                    daftarBuku.add(data);
                }
            }
        } catch (IOException e) {
            System.out.println("Gagal membaca file buku.");
        }
    }

    static void bacaAnggotaCSV() {
        daftarAnggota.clear();
        File file = new File(FILE_ANGGOTA);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length == 3) {
                    daftarAnggota.add(data);
                }
            }
        } catch (IOException e) {
            System.out.println("Gagal membaca file anggota.");
        }
    }

    static void bacaPinjamCSV() {
        daftarPinjam.clear();
        File file = new File(FILE_PINJAM);
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",", -1);
                if (data.length == 5) {
                    daftarPinjam.add(data);
                }
            }
        } catch (IOException e) {
            System.out.println("Gagal membaca file peminjaman.");
        }
    }

    // =========================
    // SIMPAN CSV
    // =========================
    static void simpanBukuCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_BUKU))) {
            for (String[] b : daftarBuku) {
                writer.write(String.join(",", b));
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data buku.");
        }
    }

    static void simpanAnggotaCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_ANGGOTA))) {
            for (String[] a : daftarAnggota) {
                writer.write(String.join(",", a));
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data anggota.");
        }
    }

    static void simpanPinjamCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PINJAM))) {
            for (String[] p : daftarPinjam) {
                writer.write(String.join(",", p));
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data peminjaman.");
        }
    }

    // =========================
    // VALIDASI DAN UTILITAS
    // =========================
    static boolean isAngka(String teks) {
        try {
            Integer.parseInt(teks);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    static boolean adaKosong(String... data) {
        for (String s : data) {
            if (s == null || s.trim().isEmpty())
                return true;
        }
        return false;
    }

    static boolean mengandungKoma(String... data) {
        for (String s : data) {
            if (s != null && s.contains(","))
                return true;
        }
        return false;
    }

    void pesanInfo(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    void pesanError(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Error", JOptionPane.ERROR_MESSAGE);
    }

    JButton tombol(String teks, Color warna) {
        JButton btn = new JButton(teks);
        btn.setFocusPainted(false);
        btn.setBackground(warna);
        btn.setForeground(Color.BLACK);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(105, 34));
        btn.setBorder(new EmptyBorder(6, 8, 6, 8));
        return btn;
    }

    void aturTampilanTabel(JTable tabel) {
        tabel.setRowHeight(28);
        tabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabel.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabel.getTableHeader().setBackground(warnaPanel);
        tabel.getTableHeader().setForeground(warnaGelap);
        tabel.setSelectionBackground(warnaUtama);
        tabel.setSelectionForeground(Color.WHITE);
    }
}
