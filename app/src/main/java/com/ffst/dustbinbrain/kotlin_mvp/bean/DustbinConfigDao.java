package com.ffst.dustbinbrain.kotlin_mvp.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "DUSTBIN_CONFIG".
*/
public class DustbinConfigDao extends AbstractDao<DustbinConfig, String> {

    public static final String TABLENAME = "DUSTBIN_CONFIG";

    /**
     * Properties of entity DustbinConfig.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property DustbinDeviceId = new Property(0, String.class, "dustbinDeviceId", true, "DUSTBIN_DEVICE_ID");
        public final static Property DustbinDeviceName = new Property(1, String.class, "dustbinDeviceName", false, "DUSTBIN_DEVICE_NAME");
        public final static Property HaasCalibration = new Property(2, boolean.class, "haasCalibration", false, "HAAS_CALIBRATION");
        public final static Property DustbinDeviceRemark = new Property(3, String.class, "dustbinDeviceRemark", false, "DUSTBIN_DEVICE_REMARK");
        public final static Property Longitude = new Property(4, double.class, "longitude", false, "LONGITUDE");
        public final static Property Latitude = new Property(5, double.class, "latitude", false, "LATITUDE");
        public final static Property HasVendingMachine = new Property(6, boolean.class, "hasVendingMachine", false, "HAS_VENDING_MACHINE");
    }


    public DustbinConfigDao(DaoConfig config) {
        super(config);
    }
    
    public DustbinConfigDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"DUSTBIN_CONFIG\" (" + //
                "\"DUSTBIN_DEVICE_ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: dustbinDeviceId
                "\"DUSTBIN_DEVICE_NAME\" TEXT," + // 1: dustbinDeviceName
                "\"HAAS_CALIBRATION\" INTEGER NOT NULL ," + // 2: haasCalibration
                "\"DUSTBIN_DEVICE_REMARK\" TEXT," + // 3: dustbinDeviceRemark
                "\"LONGITUDE\" REAL NOT NULL ," + // 4: longitude
                "\"LATITUDE\" REAL NOT NULL ," + // 5: latitude
                "\"HAS_VENDING_MACHINE\" INTEGER NOT NULL );"); // 6: hasVendingMachine
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DUSTBIN_CONFIG\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DustbinConfig entity) {
        stmt.clearBindings();
 
        String dustbinDeviceId = entity.getDustbinDeviceId();
        if (dustbinDeviceId != null) {
            stmt.bindString(1, dustbinDeviceId);
        }
 
        String dustbinDeviceName = entity.getDustbinDeviceName();
        if (dustbinDeviceName != null) {
            stmt.bindString(2, dustbinDeviceName);
        }
        stmt.bindLong(3, entity.getHaasCalibration() ? 1L: 0L);
 
        String dustbinDeviceRemark = entity.getDustbinDeviceRemark();
        if (dustbinDeviceRemark != null) {
            stmt.bindString(4, dustbinDeviceRemark);
        }
        stmt.bindDouble(5, entity.getLongitude());
        stmt.bindDouble(6, entity.getLatitude());
        stmt.bindLong(7, entity.getHasVendingMachine() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DustbinConfig entity) {
        stmt.clearBindings();
 
        String dustbinDeviceId = entity.getDustbinDeviceId();
        if (dustbinDeviceId != null) {
            stmt.bindString(1, dustbinDeviceId);
        }
 
        String dustbinDeviceName = entity.getDustbinDeviceName();
        if (dustbinDeviceName != null) {
            stmt.bindString(2, dustbinDeviceName);
        }
        stmt.bindLong(3, entity.getHaasCalibration() ? 1L: 0L);
 
        String dustbinDeviceRemark = entity.getDustbinDeviceRemark();
        if (dustbinDeviceRemark != null) {
            stmt.bindString(4, dustbinDeviceRemark);
        }
        stmt.bindDouble(5, entity.getLongitude());
        stmt.bindDouble(6, entity.getLatitude());
        stmt.bindLong(7, entity.getHasVendingMachine() ? 1L: 0L);
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public DustbinConfig readEntity(Cursor cursor, int offset) {
        DustbinConfig entity = new DustbinConfig( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // dustbinDeviceId
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // dustbinDeviceName
            cursor.getShort(offset + 2) != 0, // haasCalibration
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // dustbinDeviceRemark
            cursor.getDouble(offset + 4), // longitude
            cursor.getDouble(offset + 5), // latitude
            cursor.getShort(offset + 6) != 0 // hasVendingMachine
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, DustbinConfig entity, int offset) {
        entity.setDustbinDeviceId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setDustbinDeviceName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setHaasCalibration(cursor.getShort(offset + 2) != 0);
        entity.setDustbinDeviceRemark(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setLongitude(cursor.getDouble(offset + 4));
        entity.setLatitude(cursor.getDouble(offset + 5));
        entity.setHasVendingMachine(cursor.getShort(offset + 6) != 0);
     }
    
    @Override
    protected final String updateKeyAfterInsert(DustbinConfig entity, long rowId) {
        return entity.getDustbinDeviceId();
    }
    
    @Override
    public String getKey(DustbinConfig entity) {
        if(entity != null) {
            return entity.getDustbinDeviceId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DustbinConfig entity) {
        return entity.getDustbinDeviceId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
