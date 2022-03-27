package com.noolart.noolartpaperplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Pay implements CommandExecutor {
    private NoolartPaperPlugin plugin;

    public Pay(NoolartPaperPlugin noolartPaperPlugin) {
        this.plugin = noolartPaperPlugin;
    }

    public static void giveMoneyToPlayer(int howmany, Player p){
        int balance;

        File balanceCheck = new File(NoolartPaperPlugin.plugin.getDataFolder() + File.separator+"users"+File.separator  + p.getName() +"_balance.csv");

        try {
            Scanner scan = new Scanner(balanceCheck);
            balance = Integer.parseInt(scan.nextLine());
            balance+=howmany;

            p.sendMessage(ChatColor.GOLD+"Баланс пополнен на " +ChatColor.BOLD+ChatColor.LIGHT_PURPLE+howmany+"$ "+ ChatColor.RESET+ ChatColor.GOLD+ "Ваш баланс: "+ ChatColor.BOLD+ChatColor.LIGHT_PURPLE +balance+"$");

            scan.close();

            FileWriter writer = new FileWriter(NoolartPaperPlugin.plugin.getDataFolder() + File.separator+"users"+File.separator  + p.getName() +"_balance.csv", false);
            writer.write(Integer.toString(balance));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length!=1){
            return false;
        }
        giveMoneyToPlayer(Integer.parseInt(args[0]),Bukkit.getPlayer(sender.getName()));


        return true;
    }
}