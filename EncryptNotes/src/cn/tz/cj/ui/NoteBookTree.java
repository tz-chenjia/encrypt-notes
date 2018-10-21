package cn.tz.cj.ui;

import cn.tz.cj.bo.Auth;
import cn.tz.cj.entity.Note;
import cn.tz.cj.entity.NoteBook;
import cn.tz.cj.service.ConfigsService;
import cn.tz.cj.service.NoteBookService;
import cn.tz.cj.service.NoteService;
import cn.tz.cj.service.intf.INoteBookService;
import cn.tz.cj.service.intf.INoteService;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class NoteBookTree extends JTree {
    private static final String ROOTNODE_NAME = "NoteBooks";
    private static final Color SELECTED_BACKGROUD = new Color(115,115,115);
    private static NoteBookTree nbTree;
    private MainForm mainForm;
    private NoteBookTree(){
    }
    public static NoteBookTree getInstance(MainForm mainForm){
        if (nbTree == null) {
            synchronized (Auth.class) {
                if (nbTree == null) {
                    nbTree = new NoteBookTree();
                    nbTree.bindEvent(); //监听事件是在初始化时绑定一次
                    nbTree.mainForm = mainForm;
                }
            }
        }
        return nbTree;
    }

    private String key;
    private String lastSelectedNotebook;
    private String lastSelectedNote;

    private INoteBookService noteBookService = new NoteBookService();
    private INoteService noteService = new NoteService();

    public static void initTree(MainForm mainForm){
        NoteBookTree instance = NoteBookTree.getInstance(mainForm);
        instance.refresh(null,null,null);
    }

    public void refresh(String key, String lastSelectedNotebook, String lastSelectedNote){
        this.key = key;
        this.lastSelectedNotebook = lastSelectedNotebook;
        this.lastSelectedNote = lastSelectedNote;
        loadTree(lastSelectedNotebook, lastSelectedNote);
        if(this.lastSelectedNotebook != null && this.lastSelectedNote != null){
            // 自动显示对应内容
            Note note = noteService.getNote(this.lastSelectedNotebook, this.lastSelectedNote);
            mainForm.getjWebBrowser().setHTMLContent(note.getContent());
            mainForm.getNoteLabel().setText(this.lastSelectedNote);
        }else {
            mainForm.getjWebBrowser().setHTMLContent("");
            mainForm.getNoteLabel().setText("");
        }
    }

    private void loadTree(String lastSelectedNotebook, String lastSelectedNote){
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(ROOTNODE_NAME);
        List<NoteBook> noteBooks = noteBookService.getNoteBooks();
        Collections.sort(noteBooks);
        if(key == null || key.trim().equals("")){
            for(NoteBook nb : noteBooks){
                String notebook = nb.getNotebook();
                DefaultMutableTreeNode notebookNode = new DefaultMutableTreeNode(notebook);
                Set<String> notesTitlesByNoteBook = noteService.getNotesTitlesByNoteBook(notebook);
                for(String title : notesTitlesByNoteBook){
                    DefaultMutableTreeNode noteNode = new DefaultMutableTreeNode(title);
                    notebookNode.add(noteNode);
                }
                rootNode.add(notebookNode);
            }
        }else {
            boolean isRecordFirstSelected = false;
            for(NoteBook nb : noteBooks){
                String notebook = nb.getNotebook();
                DefaultMutableTreeNode notebookNode = new DefaultMutableTreeNode(notebook);
                Set<String> notesTitlesByNoteBook = noteService.getNotesTitlesByNoteBook(notebook);
                boolean match = false;
                for(String title : notesTitlesByNoteBook){
                    String a = title.toLowerCase();
                    String b = key.toLowerCase();
                    if(a.contains(b)){
                        DefaultMutableTreeNode noteNode = new DefaultMutableTreeNode(title);
                        if(!isRecordFirstSelected){
                            if(lastSelectedNote == null && lastSelectedNotebook == null){
                                this.lastSelectedNote = title;
                                this.lastSelectedNotebook = notebook;
                            }else{
                                this.lastSelectedNote = lastSelectedNote;
                                this.lastSelectedNotebook = lastSelectedNotebook;
                            }
                            isRecordFirstSelected = true;
                        }
                        notebookNode.add(noteNode);
                        match = true;
                    }
                }
                if(match || notebook.toLowerCase().contains(key.toLowerCase())){
                    rootNode.add(notebookNode);
                }
            }
        }
        TreeModel treeModel = new DefaultTreeModel(rootNode);
        this.setModel(treeModel);
        treeRender(rootNode);
    }

    private void bindEvent(){
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                TreePath selPath   =   NoteBookTree.this.getPathForLocation(e.getX(),   e.getY());
                if(e.getButton() == e.BUTTON3){
                    if(selPath != null){
                        int level = selPath.getPathCount();
                        String sel = selPath.getLastPathComponent().toString();
                        if(level == 1){
                            treeMenu().show(e.getComponent(), e.getX(), e.getY());//弹出右键菜单
                        }else if(level == 2){
                            noteBookMenu(sel).show(e.getComponent(), e.getX(), e.getY());//弹出右键菜单
                        }else if(level == 3){
                            Object[] path = selPath.getPath();
                            noteMenu(path[1].toString(),sel).show(e.getComponent(), e.getX(), e.getY());//弹出右键菜单
                        }
                    }else {
                        treeMenu().show(e.getComponent(), e.getX(), e.getY());//弹出右键菜单
                    }
                }else if(e.getButton() == e.BUTTON1){
                    if(selPath != null){
                        int level = selPath.getPathCount();
                    Object[] path = selPath.getPath();
                    if(level == 3){
                        //有问题，待解决
                        refresh(key, path[1].toString(), path[2].toString());
                    }else{
                        mainForm.getjWebBrowser().setHTMLContent("");
                        mainForm.getNoteLabel().setText("");
                    }
                }
                }
            }
        });
    }

    private JPopupMenu treeMenu(){
        JPopupMenu treeJPopupMenu = new JPopupMenu();
        JMenuItem jMenuItem_addNotebook = new JMenuItem("新建笔记本");
        jMenuItem_addNotebook.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddNotebook();
            }
        });
        treeJPopupMenu.add(jMenuItem_addNotebook);
        return treeJPopupMenu;
    }

    private JPopupMenu noteBookMenu(String notebookName){
        JPopupMenu treeJPopupMenu = new JPopupMenu();
        JMenuItem jMenuItem_addNote = new JMenuItem("新建笔记");
        jMenuItem_addNote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddNote(notebookName);
            }
        });
        JMenuItem jMenuItem_rename = new JMenuItem("重命名");
        jMenuItem_rename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = (String)JOptionPane.showInputDialog(null,"","请输入笔记本的名称",
                        JOptionPane.QUESTION_MESSAGE);
                if(noteBookService.rename(notebookName, newName) >  0){
                    initTree(mainForm);
                }
            }
        });
        JMenuItem jMenuItem_remove = new JMenuItem("删除笔记本");
        jMenuItem_remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = JOptionPane.showConfirmDialog(null, "确定删除["+notebookName+"]？", "删除笔记本", JOptionPane.YES_NO_OPTION);
                if(i == 0){
                    if(noteBookService.removeNoteBook(notebookName) > 0){
                        initTree(mainForm);
                    }
                }
            }
        });
        treeJPopupMenu.add(jMenuItem_addNote);
        treeJPopupMenu.add(jMenuItem_rename);
        treeJPopupMenu.add(jMenuItem_remove);
        return treeJPopupMenu;
    }

    private JPopupMenu noteMenu(String notebookName, String noteName){
        JPopupMenu treeJPopupMenu = new JPopupMenu();
        JMenuItem jMenuItem_updateNote = new JMenuItem("修改笔记");
        jMenuItem_updateNote.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditDialog.runEditDialog(NoteBookTree.this, notebookName, noteName);
            }
        });
        JMenuItem jMenuItem_remove = new JMenuItem("删除笔记");
        jMenuItem_remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = JOptionPane.showConfirmDialog(null, "确定删除["+noteName+"]？", "删除笔记", JOptionPane.YES_NO_OPTION);
                if(i == 0){
                    noteService.removeNote(notebookName, noteName);
                    initTree(mainForm);
                }
            }
        });
        treeJPopupMenu.add(jMenuItem_updateNote);
        treeJPopupMenu.add(jMenuItem_remove);
        return treeJPopupMenu;
    }

    private void treeRender(DefaultMutableTreeNode rootNode){
        if(key != null && !key.trim().equals("")){
            expandAll(this, new TreePath(rootNode), true);
        }
        // 设置自动选中
        findInTree();
        // 树样式渲染
        //this.setShowsRootHandles(false);
        this.setCellRenderer(new MyTreeCellRenderer());
        this.addFocusListener(fl);
        this.setOpaque(false);
    }

    /**
     * 定位选中的笔记
     */
    private void findInTree() {
        if(this.lastSelectedNotebook == null || this.lastSelectedNote == null){
            return;
        }
        Object root = this.getModel().getRoot();
        TreePath treePath = new TreePath(root);
        treePath = findInPath(treePath, this.lastSelectedNotebook, this.lastSelectedNote);
        if (treePath != null) {
            this.setSelectionPath(treePath);
            this.scrollPathToVisible(treePath);
        }
    }

    private TreePath findInPath(TreePath treePath, String notebookName, String noteName) {
        Object object = treePath.getLastPathComponent();
        if (object == null) {
            return null;
        }
        String value = object.toString();
        if (noteName.equals(value) && treePath.getParentPath().getLastPathComponent().toString().equals(notebookName)) {
            return treePath;
        } else {
            TreeModel model = this.getModel();
            int n = model.getChildCount(object);
            for (int i = 0; i < n; i++) {
                Object child = model.getChild(object, i);
                TreePath path = treePath.pathByAddingChild(child);

                path = findInPath(path, notebookName, noteName);
                if (path != null) {
                    return path;
                }
            }
            return null;
        }
    }

    /**
     * 全部展开
     * @param tree
     * @param parent
     * @param expand
     */
    private static void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode) e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

    public void onAddNotebook(){
        String typeName = (String)JOptionPane.showInputDialog(null,"","请输入笔记本的名称",
                JOptionPane.QUESTION_MESSAGE);
        if(typeName != null){
            if(!typeName.equals("")){
                noteBookService.addNoteBook(typeName);
                initTree(mainForm);
            }else {
                JOptionPane.showMessageDialog(null, "笔记本名称不能为空！", "创建笔记本失败", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void onAddNote(String notebookName){
        EditDialog.runEditDialog(NoteBookTree.this, notebookName, null);
    }

    FocusListener fl = new FocusListener() {
        @Override public void focusGained(FocusEvent e) {
            e.getComponent().repaint();
        }
        @Override public void focusLost(FocusEvent e) {
            e.getComponent().repaint();
        }
    };

    @Override public void paintComponent(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        if (getSelectionCount() > 0) {
            g.setColor(SELECTED_BACKGROUD);
            for (int i : getSelectionRows()) {
                Rectangle r = getRowBounds(i);
                g.fillRect(0, r.y, getWidth(), r.height);
            }
        }
        super.paintComponent(g);
        if (getLeadSelectionPath() != null) {
            Rectangle r = getRowBounds(getRowForPath(getLeadSelectionPath()));
            g.setColor(SELECTED_BACKGROUD);
            g.drawRect(0, r.y, getWidth() - 1, r.height - 1);
        }
    }
}
class MyTreeCellRenderer extends DefaultTreeCellRenderer{
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        //执行父类原型操作
        JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);
        l.setBackground(selected ? new Color(115,115,115)
                : tree.getBackground());
        l.setOpaque(true);
        setText(value.toString());
        this.setIcon(null);

        //得到每个节点的TreeNode
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
        int level = node.getLevel();
        switch (level){
            case 1:
                //System.out.println(getFont());
                setFont(new Font("SimSun",Font.PLAIN,12));
                break;
            case 2:
                setFont(new Font("SimSun",Font.PLAIN,12));
                break;
                default:
                    setFont(new Font("SimSun",Font.PLAIN,14));
                    this.setIcon(new ImageIcon(ConfigsService.getImage("tree-notebooks.png")));
                    break;
        }
        tree.setRowHeight(25);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        return this;
    }
}
