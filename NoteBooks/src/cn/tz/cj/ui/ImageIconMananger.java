package cn.tz.cj.ui;

import cn.tz.cj.service.ConfigsService;

import javax.swing.*;
import java.awt.*;

public enum ImageIconMananger {
    ABOUT("about-btn.png"), NOTEBOOK("addnotebook-btn.png"), LOGINOUT("loginout-btn.png"), NOTE("addnote-btn.png"),
    DELETE("deleteuser-btn.png"), EDIT("addnote-btn.png"), RENAME("rename-btn.png"), EDITUSER("edituser-btn.png"),
    BACKUP("email-btn.png"), EXP("exp-btn.png"), IMP("imp-btn.png"), RECOVER("recover-btn.png"), LOGO("logo.png"),
    LOGINBACKGROUD("loginbackgroud.png"),EXPANDED("expanded.png"),COLLAPSED("collapsed.png");

    private String fileName;

    private ImageIconMananger(String fileName) {
        this.fileName = fileName;
    }

    public Image getImage() {
        return ConfigsService.getImage(this.fileName);
    }

    public ImageIcon getImageIcon() {
        ImageIcon icon = new ImageIcon(getImage());
        return icon;
    }

    public ImageIcon getImageIcon20_20() {
        ImageIcon icon = new ImageIcon(getImage());
        icon.setImage(icon.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
        return icon;
    }

    public ImageIcon getImageIcon15_15() {
        ImageIcon icon = new ImageIcon(getImage());
        icon.setImage(icon.getImage().getScaledInstance(15, 15, Image.SCALE_DEFAULT));
        return icon;
    }

}
