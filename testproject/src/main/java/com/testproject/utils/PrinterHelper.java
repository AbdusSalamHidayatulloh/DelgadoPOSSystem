package com.testproject.utils;

import com.testproject.model.Transaksi;
import com.testproject.ui.transaksi.ItemKeranjang;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.util.List;

public class PrinterHelper {

    public static void cetakStruk(Transaksi t, List<ItemKeranjang> keranjang) {
        String namaToko = StoreConfig.getStoreName();
        StringBuilder struk = new StringBuilder();

        // Menyusun teks struk baris demi baris (\n = enter)
        struk.append("================================\n");
        struk.append(centerText(namaToko)).append("\n");
        struk.append("================================\n");
        struk.append("Tgl  : ").append(t.getTanggal()).append("\n");
        struk.append("Nama : ").append(t.getNamaPelanggan()).append("\n");
        struk.append("Tipe : ").append(t.getTipePesanan()).append("\n");
        struk.append("--------------------------------\n");

        for (ItemKeranjang item : keranjang) {
            String namaMenu = item.getMenu().getTipe().equals("Kustom") ? item.getKeteranganOpsi() : item.getNamaMenu();
            
            if (namaMenu.contains(" (Rp")) namaMenu = namaMenu.substring(0, namaMenu.indexOf(" (Rp"));
            if (namaMenu.length() > 32) namaMenu = namaMenu.substring(0, 32);
            
            struk.append(namaMenu).append("\n");

            String detailQty = item.getJumlah() + " x " + formatRp(item.getMenu().getHarga());
            String subtotalStr = formatRp(item.getSubtotal());
            
            // Perhitungan spasi otomatis agar subtotal rata kanan
            int spaceLength = 32 - detailQty.length() - subtotalStr.length();
            if (spaceLength < 1) spaceLength = 1;
            StringBuilder spaces = new StringBuilder();
            for(int i=0; i<spaceLength; i++) spaces.append(" ");
            
            struk.append(detailQty).append(spaces.toString()).append(subtotalStr).append("\n");

            if (!item.getMenu().getTipe().equals("Kustom") && !item.getKeteranganOpsi().isEmpty()) {
                struk.append("  +").append(item.getKeteranganOpsi()).append("\n");
            }
        }

        struk.append("--------------------------------\n");
        struk.append("Total  : ").append(formatRp(t.getTotalHarga())).append("\n");
        struk.append("Bayar  : ").append(t.getMetodeBayar()).append("\n");
        struk.append("Status : ").append(t.getStatusPembayaran()).append("\n");
        struk.append("================================\n");
        struk.append(centerText("TERIMA KASIH")).append("\n");
        
        // Menambahkan Enter ekstra di bawah agar kertas naik untuk disobek
        struk.append("\n\n\n\n\n"); 

        // Eksekusi tembak ke printer
        printRawText(struk.toString());
    }

    private static void printRawText(String text) {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
        
        if (services.length == 0) {
            System.out.println("Tidak ada printer ditemukan.");
            return;
        }

        // --- SOLUSI ERROR: Membuat atribut kosong agar tidak null ---
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();

        // Memunculkan kotak dialog pemilihan printer dengan atribut yang sah
        PrintService service = ServiceUI.printDialog(null, 200, 200, services, defaultService, null, attributes);
        
        if (service != null) {
            try {
                DocPrintJob job = service.createPrintJob();
                byte[] bytes = text.getBytes();
                Doc doc = new SimpleDoc(bytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
                
                job.print(doc, attributes);
            } catch (Exception e) {
                System.out.println("Gagal print struk: " + e.getMessage());
            }
        }
    }

    private static String formatRp(double nominal) {
        return String.format("Rp %,.0f", nominal).replace(",", ".");
    }

    private static String centerText(String text) {
        int width = 32; 
        if (text.length() >= width) return text.substring(0, width);
        int padding = (width - text.length()) / 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < padding; i++) sb.append(" ");
        sb.append(text);
        return sb.toString();
    }
}