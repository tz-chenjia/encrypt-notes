package cn.tz.cj.service;

import cn.tz.cj.dao.NoteDao;
import cn.tz.cj.entity.Note;
import cn.tz.cj.service.intf.INoteService;

import javax.swing.*;
import java.util.Set;

public class NoteService implements INoteService {

    private NoteDao noteDao = new NoteDao();

    @Override
    public int addNote(String noteBookName, String title, String content) {
        return noteDao.insertNoteDao(buildNote(noteBookName, title, content));
    }

    @Override
    public int removeNote(String noteBookName, String title) {
        return noteDao.deleteNoteDao(buildNote(noteBookName, title, ""));
    }

    @Override
    public int updateNote(String oldNoteBookName, String oldTitle, String noteBookName, String title, String content) {
        return noteDao.updateNoteDao(buildNote(oldNoteBookName,oldTitle,null), buildNote(noteBookName,title,content));
    }

    @Override
    public Note getNote(String noteBookName, String title) {
        return noteDao.getNote(buildNote(noteBookName, title, ""));
    }

    @Override
    public int getNotesNumWithNoteBook(String noteBookName) {
        return noteDao.getNotesNumWithNoteBook(noteBookName);
    }

    @Override
    public int updateNoteBookByNote(String noteBookName, String newName) {
        return noteDao.updateNoteBookByNote(noteBookName,newName);
    }

    @Override
    public Set<String> getNotesTitlesByNoteBook(String noteBookName) {
        return noteDao.getNotesTitlesByNoteBook(noteBookName);
    }

    private Note buildNote(String noteBookName, String title, String content){
        Note note = new Note();
        note.setNotebook(noteBookName);
        note.setTitle(title);
        note.setContent(content);
        return note;
    }

    @Override
    public boolean checkTitleExists(String noteBookName, String title){
        Note note = noteDao.getNote(buildNote(noteBookName, title, ""));
        return note != null;
    }

}
