package cn.tz.cj.ui;

import cn.tz.cj.entity.NoteBook;
import cn.tz.cj.service.ConfigsService;
import cn.tz.cj.service.NoteBookService;
import cn.tz.cj.service.NoteService;
import cn.tz.cj.service.intf.INoteBookService;
import cn.tz.cj.service.intf.INoteService;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.DisposeEvent;
import com.teamdev.jxbrowser.chromium.events.DisposeListener;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.StartLoadingEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import com.teamdev.jxbrowser.chromium.swing.DefaultDialogHandler;
import com.teamdev.jxbrowser.chromium.swing.DefaultNetworkDelegate;
import com.teamdev.jxbrowser.chromium.swing.DefaultPopupHandler;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.util.List;

public class Editor {

    private static final String URL = ConfigsService.getConfPath() + "summer" + File.separator + "index.html";

    private Browser browser = new Browser();

    private BrowserView browserView = new BrowserView(browser);

    private NoteBookTree nbTree;

    private JComboBox noteBook;

    private JTextField note;

    private INoteBookService noteBookService = new NoteBookService();
    private INoteService noteService = new NoteService();

    private boolean isNewAdd;
    private String oldNoteBookName;
    private String oldNoteName;

    public Editor(NoteBookTree nbTree, JComboBox noteBook, JTextField note){
        this.nbTree = nbTree;
        this.noteBook = noteBook;
        this.note = note;
    }

    public Browser getBrowser() {
        return browser;
    }

    public BrowserView getBrowserView() {
        return browserView;
    }

    public void initEditor(boolean isNewAdd){
        this.isNewAdd = isNewAdd;
        this.browserView.setDragAndDropEnabled(false);
        this.browser.setPopupHandler(new DefaultPopupHandler(){
            @Override
            public PopupContainer handlePopup(PopupParams params) {
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI(params.getURL()));
                        } catch (Exception ex) {
                            //return super.handlePopup(params);
                        }
                        return null;
                    }
                }else {
                    return super.handlePopup(params);
                }
                return super.handlePopup(params);
            }
        });
        this.browser.loadURL(URL);
    }

    public void refresh(String noteBookName, String noteName, String htmlContent){
        noteBookName = noteBookName != null ? noteBookName : "";
        noteName = noteName != null ? noteName : "";
        this.oldNoteBookName = noteBookName;
        this.oldNoteName = noteName;
        this.noteBook.removeAllItems();
        List<NoteBook> noteBooks = noteBookService.getNoteBooks();
        for (NoteBook nb : noteBooks) {
            this.noteBook.addItem(nb.getNotebook());
        }
        this.noteBook.setSelectedItem(noteBookName);
        this.note.setText(noteName);
        if(htmlContent != null && !htmlContent.equals("")) setHTMLContent(htmlContent);
    }

    private void setHTMLContent(String htmlContent){
        do {
            this.browser.executeJavaScript("$(\"div#summernote\").summernote(\"code\",\""+htmlContent+"\")");
        }while (getHTMLContent().equals("GET_HTML_CONTENT_FAIL"));
    }

    private String getHTMLContent(){
        JSValue jsValue = this.browser.executeJavaScriptAndReturnValue("$(\"div#summernote\").summernote(\"code\")");
        String stringValue;
        try {
            stringValue = jsValue.getStringValue().replace("\"","\\\"");
        }catch (IllegalStateException e){
            stringValue = "GET_HTML_CONTENT_FAIL";
        }
        return stringValue;
    }

    public void save(){
        String newNoteName = note.getText();
        String newNoteBookName = noteBook.getSelectedItem() == null ?"": noteBook.getSelectedItem().toString();
        String htmlContent = "";
        do{
            htmlContent = getHTMLContent();
        }while (htmlContent.equals("GET_HTML_CONTENT_FAIL"));
        if(!newNoteBookName.trim().equals("") && !newNoteName.trim().equals("")){
            if(isNewAdd){
                noteService.addNote(newNoteBookName,newNoteName,htmlContent);
            }else {
                noteService.updateNote(oldNoteBookName,oldNoteName,newNoteBookName,newNoteName,htmlContent);
                JOptionPane.showMessageDialog(null, "保存成功");
            }
            nbTree.refresh(null, newNoteBookName, newNoteName);
        }
    }

}
