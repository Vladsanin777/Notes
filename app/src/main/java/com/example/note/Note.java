package com.example.note;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.example.note.TypeNote.*;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Note {
    private static final String HEADS_HOTES = "HEADS_NOTE";
    private static final String DELETEDS_HOTES = "DELETED_NOTE";
    private static final String TEMPLATES_HOTES = "TEMPLATE_NOTE";
    private static final String HEAD_FILE = "HEAD";
    private static final String PREFIX = "note:";
    private static final ArrayList<Note> m_headsNotes = new ArrayList<Note>();
    private static final ArrayList<Note> m_templateNotes = new ArrayList<Note>();
    private static final ArrayList<Note> m_deletedNotes = new ArrayList<Note>();
    private static final ZoneId m_deviceZone = ZoneId.systemDefault();
    private static final Map<String, Note> m_allNotes = new HashMap<String, Note>();
    private static Context m_context;
    private int m_indexNote;
    private String m_parentHash;
    private String m_hash;
    private Note m_parent;
    private String m_name;
    private String m_content;
    private Instant m_time;
    private boolean m_isRenamed;
    private boolean m_isEdited;
    private TypeNote m_type;

    public static void INIT(Context context) {
        setContext(context);

        INIT_ALL_NOTE();

        INIT_HEADS_NOTES();
        INIT_TEMPLATE_HOTES();
        INIT_DELEED_HOTES();

        INIT_PARENT_HEADS_NOTES();
        INIT_PARENT_TEMPLATE_HOTES();
        INIT_PARENT_DELEED_HOTES();
    }

    private static void INIT_ALL_NOTE() {
        File prefsDir = new File(m_context.getApplicationInfo().dataDir, "shared_prefs");
        if (prefsDir.exists() && prefsDir.isDirectory()) {
            String[] list = prefsDir.list();

            for (String fileName : list) {
                if (fileName.endsWith(".xml")) {
                    String nameForPrefs = fileName.substring(0, fileName.lastIndexOf(".xml"));

                    Note note = deserialize(nameForPrefs);

                    if (note != null) {
                        m_allNotes.put(nameForPrefs.substring(5), note);
                    }
                }
            }
        }
    }

    public static void INIT_DELEED_HOTES() {
        INIT_BASE(HEAD_FILE, DELETEDS_HOTES, m_deletedNotes);
    }

    public static void INIT_TEMPLATE_HOTES() {
        INIT_BASE(HEAD_FILE, TEMPLATES_HOTES, m_templateNotes);
    }

    public static void INIT_HEADS_NOTES() {
        INIT_BASE(HEAD_FILE, HEADS_HOTES, m_headsNotes);
    }

    public static void INIT_BASE(String fileName, String key, ArrayList<Note> list) {
        SharedPreferences prefs = m_context.getSharedPreferences(fileName, Context.MODE_PRIVATE);

        Set<String> hashs = prefs.getStringSet(key, null);

        Log.d("key", key);

        if (hashs != null) {
            for (String hash : hashs) {
                Log.d("hash", hash);
                list.add(m_allNotes.get(hash.substring(5)));
            }
        }
    }

    public static void INIT_PARENT_DELEED_HOTES() {
        INIT_PARENT_BASE(m_deletedNotes);
    }

    public static void INIT_PARENT_TEMPLATE_HOTES() {
        INIT_PARENT_BASE(m_templateNotes);
    }

    public static void INIT_PARENT_HEADS_NOTES() {
        INIT_PARENT_BASE(m_headsNotes);
    }
    public static void INIT_PARENT_BASE(ArrayList<Note> list) {
        for (Note child : list) {
            while (child != null && child.m_parentHash != null && child.m_parent == null) {
                child.m_parent = m_allNotes.get(child.m_parentHash);
                child = child.m_parent;
            }
        }
    }

    private static void UPDATE_HEADS() {
        Log.d("update", "HEADS");
        UPDATE_BASE(HEAD_FILE, HEADS_HOTES, m_headsNotes);
    }

    private static void UPDATE_TEMPLATE() {
        Log.d("update", "TEMPLATE");
        UPDATE_BASE(HEAD_FILE, TEMPLATES_HOTES, m_templateNotes);
    }

    private static void UPDATE_DELETED() {
        Log.d("update", "DELEDED");
        UPDATE_BASE(HEAD_FILE, DELETEDS_HOTES, m_deletedNotes);
    }

    private static void UPDATE_BASE(String fileName, String key, ArrayList<Note> list) {
        SharedPreferences prefs = m_context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Set<String> headsToSave = new HashSet<String>();

        for (int i = 0; i < list.size(); i++) {
            Note note = list.get(i);
            if (note != null) {
                Log.d("hash", note.m_hash);
                headsToSave.add(PREFIX + note.m_hash);
            }
        }

        editor.putStringSet(key, headsToSave);
        editor.apply();
    }

    public Note(String name, String content, TypeNote type) {
        m_name = name;
        m_content = content;
        m_time = now();
        m_isRenamed = false;
        m_isEdited = false;
        m_type = type;
        updateHash();
        m_allNotes.put(m_hash, this);
        switch (type) {
            case HEAD:
                head();
                break;
            case TEMPLATE:
                template();
                break;
            case DELETED:
                delete();
                break;
        }
    }

    public Note(String name, String content, TypeNote type, Note parent) {
        m_name = name;
        m_content = content;
        m_time = now();
        if (parent != null) {
            m_parentHash = parent.m_hash;
            m_parent = parent;
            m_isRenamed = isRenamed(parent);
            m_isEdited = isEdited(parent);
            if (parent.m_type == TypeNote.HEAD) {
                m_headsNotes.set(parent.m_indexNote, null);
                UPDATE_HEADS();
            }
        } else {
            m_isRenamed = false;
            m_isEdited = false;
        }

        updateHash();

        m_allNotes.put(m_hash, this);

        switch (type) {
            case HEAD:
                head();
                break;
            case TEMPLATE:
                template();
                break;
            case DELETED:
                delete();
                break;
        }
    }

    protected Note(SharedPreferences in) {
        parce(in);
    }

    private void parce(SharedPreferences in) {
        m_parentHash = in.getString("parentHash", null);
        m_name = in.getString("name", null);
        m_content = in.getString("content", null);
        long millis = in.getLong("time", -1);
        m_time = (millis != -1) ? Instant.ofEpochMilli(millis) : null;
        m_isRenamed = in.getBoolean("renamed", false);
        m_isEdited = in.getBoolean("edited", false);
        m_type = TypeNote.values()[in.getInt("type", TypeNote.HEAD.ordinal())];
        updateHash();
    }

    private void unparce(SharedPreferences out) {
        SharedPreferences.Editor editor = out.edit();

        Log.d("unparce", m_hash);

        editor.putString("parentHash", m_parentHash);
        editor.putString("name", m_name);
        editor.putString("content", m_content);
        editor.putLong("time", m_time != null ? m_time.toEpochMilli() : -1);
        editor.putBoolean("renamed", m_isRenamed);
        editor.putBoolean("edited", m_isEdited);
        editor.putInt("type", m_type.ordinal());

        editor.apply();
    }

    private String generateHash() {
        byte[] nameBytes = null;
        byte[] contentBytes = null;
        byte[] parentHashBytes = null;
        byte[] timeBytes = null;

        MessageDigest md = null;

        try {
            if (m_name != null)
                nameBytes = m_name.getBytes("UTF-8");
            if (m_content != null)
                contentBytes = m_content.getBytes("UTF-8");
            if (m_parentHash != null)
                parentHashBytes = m_parentHash.getBytes("UTF-8");
            if (m_time != null)
                timeBytes = String.valueOf(m_time.getEpochSecond()).getBytes(StandardCharsets.UTF_8);

            md = MessageDigest.getInstance("SHA-1");
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

        if (nameBytes != null) md.update(nameBytes);
        if (contentBytes != null) md.update(contentBytes);
        if (parentHashBytes != null) md.update(parentHashBytes);
        if (timeBytes != null) md.update(timeBytes);

        byte[] hashBytes = md.digest();

        return bytesToHex(hashBytes);
    }

    private void updateHash() {
        String hash = generateHash();
        if (hash != null) {
            m_hash = hash;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private void serialize() {
        Log.d("serialize", m_hash);
        if (m_hash != null) {

            SharedPreferences prefs = m_context.getSharedPreferences(PREFIX + m_hash, Context.MODE_PRIVATE);

            unparce(prefs);
        }
    }

    public static Note deserialize(String fileName) {
        if (!fileName.startsWith(PREFIX))
            return null;
        SharedPreferences prefs = m_context.getSharedPreferences(fileName, Context.MODE_PRIVATE);

        Note note = new Note(prefs);
        if (fileName.substring(5).equals(note.m_hash)) {
            return note;
        } else {
            return null;
        }
    }

    public static int getHeadCount() {
        return m_headsNotes.size();
    }

    public static int getTemplateCount() {
        return m_templateNotes.size();
    }
    public static int getDeletedCount() {
        return m_deletedNotes.size();
    }

    public static Note getHeadNote(int i) {
        return m_headsNotes.get(i);
    }

    public static Note getTemplateNote(int i) {
        return m_templateNotes.get(i);
    }

    public static Note getDeletedNote(int i) {
        return m_deletedNotes.get(i);
    }

    public String getName() {
        return m_name;
    }
    public String getContent() {
        return m_content;
    }
    public ZonedDateTime getTime() {
        return m_time.atZone(m_deviceZone);
    }

    public boolean isRenamed() {
        return m_isRenamed;
    }

    public boolean isRenamed(String newName) {
        return !m_name.equals(newName);
    }

    public boolean isRenamed(Note note) {
        return !m_name.equals(note.m_name) || note.m_isRenamed;
    }

    public boolean isEdited() {
        return m_isEdited;
    }

    public boolean isEdited(String newContent) {
        return !m_content.equals(newContent);
    }

    public boolean isEdited(Note note) {
        return !m_content.equals(note.m_content) || note.m_isEdited;
    }


    public boolean isDeleted() {
        return m_type == TypeNote.DELETED;
    }

    public void head() {
        if (m_type != null) {
            switch (m_type) {
                case TEMPLATE:
                    if (m_templateNotes.get(m_indexNote) == this) {
                        m_templateNotes.set(m_indexNote, null);
                        UPDATE_TEMPLATE();
                    }
                    break;
                case DELETED:
                    if (m_deletedNotes.get(m_indexNote) == this) {
                        m_deletedNotes.set(m_indexNote, null);
                        UPDATE_DELETED();
                    }
                    break;
            }
        }
        m_indexNote = m_headsNotes.size();
        m_headsNotes.add(this);
        m_type = TypeNote.HEAD;
        UPDATE_HEADS();
        serialize();
    }

    public void template() {
        if (m_type != null) {
            switch (m_type) {
                case HEAD:
                    if (m_headsNotes.get(m_indexNote) == this) {
                        m_headsNotes.set(m_indexNote, null);
                        UPDATE_HEADS();
                    }
                    break;
                case DELETED:
                    if (m_deletedNotes.get(m_indexNote) == this) {
                        m_deletedNotes.set(m_indexNote, null);
                        UPDATE_DELETED();
                    }
                    break;
            }
        }
        m_indexNote = m_templateNotes.size();
        m_templateNotes.add(this);
        m_type = TypeNote.TEMPLATE;
        serialize();
        UPDATE_TEMPLATE();
    }

    public void delete() {
        if (m_type != null) {
            switch (m_type) {
                case HEAD:
                    if (m_headsNotes.get(m_indexNote) == this) {
                        m_headsNotes.set(m_indexNote, null);
                        UPDATE_HEADS();
                    }
                    break;
                case TEMPLATE:
                    if (m_templateNotes.get(m_indexNote) == this) {
                        m_templateNotes.set(m_indexNote, null);
                        UPDATE_TEMPLATE();
                    }
                    break;
            }
        }
        m_indexNote = m_deletedNotes.size();
        m_deletedNotes.add(this);
        m_type = TypeNote.DELETED;
        serialize();
        UPDATE_DELETED();
    }


    public void deleteForce() {
        if (m_type != null) {
            switch (m_type) {
                case HEAD:
                    if (m_headsNotes.get(m_indexNote) == this) {
                        m_headsNotes.set(m_indexNote, null);
                        UPDATE_HEADS();
                    }
                    break;
                case TEMPLATE:
                    if (m_templateNotes.get(m_indexNote) == this) {
                        m_templateNotes.set(m_indexNote, null);
                        UPDATE_TEMPLATE();
                    }
                    break;
                case DELETED:
                    if (m_deletedNotes.get(m_indexNote) == this) {
                        m_deletedNotes.set(m_indexNote, null);
                        UPDATE_DELETED();
                    }
                    break;
            }
        }
        String nameForPrefs = PREFIX + m_hash;

        m_context.getSharedPreferences(nameForPrefs, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();

        File prefsDir = new File(m_context.getApplicationInfo().dataDir, "shared_prefs");
        File noteFile = new File(prefsDir, nameForPrefs + ".xml");

        if (noteFile.exists()) {
            noteFile.delete();
        }
    }

    public Boolean isTemplate() {
        return m_type == TypeNote.TEMPLATE;
    }

    public Note getParent() {
        return m_parent;
    }

    public TypeNote getType() {
        return  m_type;
    }

    private static Instant now() {
        return Instant.now();
    }

    private static void setContext(Context context) {
        m_context = context;
    }

    public int getIndex() {
        return m_indexNote;
    }

    public static Note getNote(String hash) {
        if (hash != null)
            return m_allNotes.get(hash);
        return null;
    }

    public String getHash() {
        return m_hash;
    }
}