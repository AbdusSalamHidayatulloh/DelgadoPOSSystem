package com.testproject.ui.transaksi;

import com.testproject.model.MenuItem;
import java.util.List;

public class ItemKeranjang {
    private final MenuItem menu;
    private final int jumlah;
    private final double subtotal;
    private final String keteranganOpsi;
    private final List<Integer> checkedOpsiIds;
    private final List<Integer> chosenPilihanIds;

    public ItemKeranjang(MenuItem menu, int jumlah, double subtotal, String keteranganOpsi,
                         List<Integer> checkedOpsiIds, List<Integer> chosenPilihanIds) {
        this.menu = menu;
        this.jumlah = jumlah;
        this.subtotal = subtotal;
        this.keteranganOpsi = keteranganOpsi;
        this.checkedOpsiIds = checkedOpsiIds;
        this.chosenPilihanIds = chosenPilihanIds;
    }

    public MenuItem getMenu() { return menu; }
    public String getNamaMenu() { return menu.getNama(); }
    public int getJumlah() { return jumlah; }
    public double getSubtotal() { return subtotal; }
    public String getKeteranganOpsi() { return keteranganOpsi; }
    public List<Integer> getCheckedOpsiIds() { return checkedOpsiIds; }
    public List<Integer> getChosenPilihanIds() { return chosenPilihanIds; }
}