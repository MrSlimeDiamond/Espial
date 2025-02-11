package net.slimediamond.espial.nbt;


import net.kyori.adventure.text.Component;

import java.util.List;

public interface SignData {
    List<String> getFrontComponents();
    List<String> getBackComponents();
}
