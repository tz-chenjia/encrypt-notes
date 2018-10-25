package cn.tz.cj.ui;

import cn.tz.cj.entity.UserConfigs;
import cn.tz.cj.service.AuthService;
import cn.tz.cj.service.ConfigsService;
import cn.tz.cj.service.intf.IAuthService;
import cn.tz.cj.service.intf.IConfigsService;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class LoginDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton configBtn;
    private JTextField emailTextField;
    private JPasswordField pwdPasswordField;
    private JLabel emailLabel;
    private JLabel pwdLabel;
    private JPanel footerJPanel;
    private JPanel btnJPanel;
    private JPanel formJPanel;

    private IAuthService authService = new AuthService();
    private IConfigsService configsService = new ConfigsService();

    private void setBackGroudImg(){
        ImageIcon imageIcon = ImageIconMananger.LOGINBACKGROUD.getImageIcon();
        JLabel label = new JLabel(imageIcon);
        label.setBounds(0,0,imageIcon.getIconWidth(),imageIcon.getIconHeight());
        this.getLayeredPane().add(label, new Integer(Integer.MIN_VALUE));
        contentPane.setOpaque(false);
        footerJPanel.setOpaque(false);
        formJPanel.setOpaque(false);
        btnJPanel.setOpaque(false);
    }

    public LoginDialog() {
        setTitle("NoteBooks - 登录");
        setIconImage(ImageIconMananger.LOGO.getImage());
        setSize(400, 400);
        setLocationRelativeTo(null);
        setContentPane(contentPane);
        getRootPane().setDefaultButton(buttonOK);
        setBackGroudImg();

        emailTextField.setDocument(new InputLengthLimit(40));
        UserConfigs userConfigs = configsService.getUserConfigs();
        if(userConfigs != null){
            emailTextField.setText(userConfigs.getUserEmail());
        }
        pwdPasswordField.setDocument(new InputLengthLimit(40));
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        configBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DBConfigsDialog dbConfigsDialog = new DBConfigsDialog();
                dbConfigsDialog.pack();
                dbConfigsDialog.setVisible(true);
            }
        });
    }

    private void onOK() {
        if (authService.login(emailTextField.getText(), String.valueOf(pwdPasswordField.getPassword()))) {
            UserConfigs userConfigs = configsService.setUserEmail(emailTextField.getText());
            new MainForm(userConfigs);
            dispose();
        }
    }
}
