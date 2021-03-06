package org.openstreetmap.josm.plugins.imagerycache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.data.preferences.BooleanProperty;

/**
 * Class to store tile database in MapDB key-value storage
 * Used as singleton to share database-accessing onbjects between all tile loaders (all layers, etc.)
 */
public class TileDAOMapDB implements TileDAO {
    public static final boolean debug = new BooleanProperty("imagerycache.debug", false).get();
    public static final boolean noMmap = new BooleanProperty("imagerycache.randomAccessFile", false).get();
    
    public static boolean dbNotAvailable = false;

    protected HashMap<String, DB> dbs = new HashMap<String, DB>();
    protected HashMap<String, Map<Long, DBTile>> storages = new HashMap<String, Map<Long, DBTile>>();
    private File cacheFolder;
    
    
    static TileDAOMapDB instance = new TileDAOMapDB();

    public static TileDAOMapDB getInstance() {
        return instance;
    }

    /**
     * Lazy creation of DB object associated to * @param source
     * or returning from cache
     */
    private synchronized DB initDB(String source) {
        DB db = dbs.get(source);
        if (db!=null) return db;
        
        File f = null;
        for (int attempt=0; attempt<20; attempt++) {
            try {
                String fname = getDBFileName(source, attempt);
                
                
                f = new File(cacheFolder, fname);
                f.createNewFile();
                if (f.exists() && !f.canWrite()) continue;
         
                File lock;
                lock = new File(cacheFolder,  fname+".lock");
                if (!lock.createNewFile())  continue;
                lock.deleteOnExit();
                
                if (noMmap) {
                    db = DBMaker.newFileDB(f)
                        .randomAccessFileEnable()
                        .writeAheadLogDisable().closeOnJvmShutdown().make();
                } else {
                    db = DBMaker.newFileDB(f)
                        .randomAccessFileEnableIfNeeded()
                        .writeAheadLogDisable().closeOnJvmShutdown().make();
                }
                
                
                dbs.put(source, db);
                
                Map<Long, DBTile> m = db.getHashMap("tiles");
                storages.put(source, m);
                
                Main.info("Opened database file successfully: "+f.getAbsolutePath());
            } catch (Exception ex) {
                Main.warn("Error: can not create database, file: "+f.getAbsolutePath());
                //System.out.println(ex.getMessage());
                ex.printStackTrace();
                try {
                    if (db!=null) db.close();
                } catch (Exception e) { };
                throw new RuntimeException(ex);
            }
            mergeSources(source);
            return db;
        }
        
        // Fallback solution: 
        db = DBMaker.newMemoryDB().asyncWriteDisable()
            .writeAheadLogDisable().closeOnJvmShutdown().make();

        dbs.put(source, db);

        Map<Long, DBTile> m = db.getHashMap("tiles");
        storages.put(source, m);
        return db;
        
    }

    private synchronized Map<Long, DBTile> getStorage(String source) {
        if (!storages.containsKey(source)) {
            initDB(source);
        }
        Map<Long, DBTile> m = storages.get(source);
        return m;
    }

    private TileDAOMapDB() {
        
    }
    
    void setCacheFolder(File f) {
        cacheFolder = f;
    }

    @Override
    public DBTile getById(String source, long id) {
        if (dbNotAvailable) return null;
       return getStorage(source).get(id);
    }

    @Override
    public synchronized void updateModTime(String source, long id, DBTile dbTile) {
        if (debug) System.out.println("Tile " + id + ": Updating modification time");
        if (dbNotAvailable) return;
        getStorage(source).put(id, dbTile);
    }

    @Override
    public synchronized void  updateTile(String source, long id, DBTile dbTile) {
        if (debug) System.out.println("Tile " + id + ": Updating tile in base");
        if (dbNotAvailable) return;
        getStorage(source).put(id, dbTile);
    }

    @Override
    public synchronized void deleteTile(String source, long id) {
        if (dbNotAvailable) return;
        getStorage(source).remove(id);
    }
    
    

    private String getDBFileName(String source, int attempt) {
        String fname = "tiles_" + source.replaceAll("[\\\\/:*?\"<>| ]", "_");
        if (attempt > 0) fname=fname+"_"+attempt;
        return fname;
    }
    
    /**
     * Opens all files related to source @param source  as databases
     * and move their contents to currently active base
     * Used for joining the cache from multiple instances of JOSM
     */
    private void mergeSources(String source) {
        DB db = null;
        Map<Long, DBTile> myMap = storages.get(source);
        for (int t=0; t<20; t++) {
            try {
                String fname = getDBFileName(source, t);
                File f = new File(cacheFolder, fname);
                if (!f.exists() || !f.canWrite()) continue;
                
                File lock;
                lock = new File(cacheFolder, fname+".lock");
                if (lock.exists())  continue;
                
                db = DBMaker.newFileDB(f)
                    .randomAccessFileEnableIfNeeded()
                    .writeAheadLogDisable().closeOnJvmShutdown().make();
                
                
                Map<Long, DBTile> m = db.getHashMap("tiles");
                
                // Merging!
                Main.info("Moving records from "+f.getName()+" to open storage "+source);
                myMap.putAll(m);
                db.close();
                new File(cacheFolder, fname+".p").delete();
                f.delete();
                
                Main.info("Moved database successfully from file "+f.getAbsolutePath());
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                try {
                    if (db!=null) db.close();
                } catch (Exception e) { };
            }
        }
    }

    public void cleanStorage(String name) {
        Main.info("Cleaning storage: {0}", name);
        Main.info("There were {0} tiles in it", storages.get(name).size());
        try {
            dbs.get(name).close();
        } catch (Exception e) {
            Main.warn("Something may be wrong, can not close the database.");
        }

        dbs.remove(name);
        storages.remove(name);
        for (int t=0; t<20; t++) {
            String fname = getDBFileName(name, t);
            File f = new File(cacheFolder, fname);
            if (!f.exists() || !f.canWrite()) continue;
            f.delete();
            f = new File(cacheFolder, fname+".p");
            if (!f.exists() || !f.canWrite()) continue;
            f.delete();
            f = new File(cacheFolder, fname+".lock");
            if (!f.exists() || !f.canWrite()) continue;
            try {
                f.delete();
            } catch (Exception e) { Main.warn("Can not delete file, maybe it is locvked by another JOSM instance. This is not a serious error then."); }
        }
    }
}
