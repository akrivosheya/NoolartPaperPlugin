package com.noolart.noolartpaperplugin;

import com.noolart.noolartpaperplugin.commands.Commands;
import com.noolart.noolartpaperplugin.listener.MyListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ItemFrame;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.NotLinkException;

public class Slice implements CommandExecutor {

    private NoolartPaperPlugin plugin;

    public Slice(NoolartPaperPlugin noolartPaperPlugin) {
        this.plugin = noolartPaperPlugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        Location loc1 = NoolartPaperPlugin.point1.clone();
        Location loc2 = NoolartPaperPlugin.point2.add(0,-(NoolartPaperPlugin.point2.getY()),0);
        String filename = strings[0];

        if (loc1.getZ()==loc2.getZ()) {


            try (FileWriter fw = new FileWriter(NoolartPaperPlugin.plugin.getDataFolder() + File.separator + "slices\\" + filename + ".csv",false)){
                fw.write("");
                int xMax = (int) Double.max(loc1.getX(), loc2.getX());
                int yMax = (int) Double.max(loc1.getY(),loc2.getY());
                int xMin = (int) Double.min(loc1.getX(), loc2.getX());
                int yMin = (int) Double.min(loc1.getY(),loc2.getY());
                Location loc3 =  new Location(Bukkit.getPlayer(commandSender.getName()).getWorld(),xMin,yMin,loc1.getZ());
                for (int i = xMin; i < xMax; i++){
                    for (int j = yMin; j < yMax; j++) {
                        loc3.add(0, 1, 0);
                        fw.write((int)loc3.getX()+";"+(int)loc3.getY()+";"+(int)loc3.getZ()+";"+loc3.getBlock().getType().toString()+";"+"\n");
                    }
                    loc3.add(1, -(yMax - yMin), 0);
                }
                Commands.pythonrun("seism",filename);
                Location screenLocation = new Location(Bukkit.getPlayer(commandSender.getName()).getWorld(),(xMax+xMin)/2,loc1.getY()+1,loc1.getZ());
                screenLocation.getBlock().setType(Material.OBSIDIAN);
                Bukkit.getPlayer(commandSender.getName()).getWorld().spawn(screenLocation.add(0,0,1), ItemFrame.class, itemframe -> {
                    itemframe.setFacingDirection(BlockFace.SOUTH);
                    itemframe.setItem(MyListener.mapToStack(filename+".jpg",Bukkit.getPlayer(commandSender.getName()).getWorld()));

                        });
            } catch (IOException e) {
                e.printStackTrace();
            }


            Bukkit.getPlayer(commandSender.getName()).sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "Slice is done!");

        }
        if (loc1.getX()==loc2.getX()) {


            try (FileWriter fw = new FileWriter(NoolartPaperPlugin.plugin.getDataFolder() + File.separator + "slices\\" + filename + ".csv",false)){
                fw.write("");
                int zMax = (int) Double.max(loc1.getZ(), loc2.getZ());
                int yMax = (int) Double.max(loc1.getY(),loc2.getY());
                int zMin = (int) Double.min(loc1.getZ(), loc2.getZ());
                int yMin = (int) Double.min(loc1.getY(),loc2.getY());
                Location loc3 =  new Location(Bukkit.getPlayer(commandSender.getName()).getWorld(),zMin,yMin,loc1.getZ());
                for (int i = zMin; i < zMax; i++){
                    for (int j = yMin; j < yMax; j++) {
                        loc3.add(0, 1, 0);
                        fw.write(loc3.getBlock().getType().toString()+"\n");
                    }
                    loc3.add(0, -(yMax - yMin), 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Bukkit.getPlayer(commandSender.getName()).sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "Slice is done!");


        }













        return true;
    }
}
