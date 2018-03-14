package com.sistemium.sissales.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMModelMapping
import java.sql.SQLException

/**
 * Created by edgarjanvuicik on 29/11/2017.
 */
class STMSQLIteSchema(private val database: SQLiteDatabase) {

    private var modelMapping:STMModelMapping? = null
    private var migrationSuccessful = false
    private var columnsDictionary: HashMap<String, ArrayList<String>>? = null
    private val builtInAttributes = arrayListOf(
            STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY,
            STMConstants.STMPersistingKeyCreationTimestamp,
            STMConstants.STMPersistingKeyVersion,
            STMConstants.STMPersistingOptionLts,
            STMConstants.STMPersistingKeyPhantom
            )
    private val ignoredAttributes = builtInAttributes + "xid"
    private var recreatedTables = hashSetOf<String>()
    private val tablesToReload = hashSetOf<String>()

    fun createTablesWithModelMapping(modelMapping: STMModelMapping): Map<String, ArrayList<String>>{

        STMFunctions.debugLog("STMSQLIteSchema","createTablesWithModelMapping")
        this.modelMapping = modelMapping
        migrationSuccessful = true
        columnsDictionary = HashMap(currentDBScheme())

        for (entityDescription in modelMapping.addedEntities) {
            addEntity(entityDescription)
        }

        for (entityDescription in modelMapping.removedEntities){
            deleteEntity(entityDescription)
        }

        //TODO properties change
//        if (!modelMapping.removedProperties().isEmpty()){
//
//            modelMapping.removedAttributes().forEach{
//
//                if (it.value.count() == 0) return@forEach
//                recreateEntityWithName(it.key)
//
//            }
//
//            modelMapping.removedRelationships().forEach{
//
//                if (it.value.count() == 0) return@forEach
//
//                val tableName = STMFunctions.removePrefixFromEntityName(it.key)
//                if (recreatedTables.contains(tableName)) return@forEach
//
//                val filtered = it.value.filter {
//
//                    return@filter !it.isToMany
//
//                }
//
//                if (filtered.count() > 0) {
//
//                    recreateEntityWithName(it.key)
//                    return@forEach
//
//                }
//
//                val relationships = it.value.filter {
//
//                    return@filter it.isToMany
//
//                }
//
//                for (toManyRelationship in relationships){
//
//                    migrationSuccessful = executeDDL(deleteToManyRelationshipDDL(toManyRelationship, tableName)!!)
//
//                }
//
//            }
//
//        }
//
//        modelMapping.addedProperties().forEach{
//
//            addPropertiesArray(it.value, it.key)
//
//        }

        fillRecreatedTablesWithFantom()
        eTagReseting()

        return if (migrationSuccessful){

            STMFunctions.debugLog("STMSQLiteSchema","model migrating SUCCESS")

            columnsDictionary!!

        } else {

            STMFunctions.debugLog("STMSQLiteSchema","model migrating NOT SUCCESS")

            columnsDictionary!!

        }

    }

    fun currentDBScheme(): Map<String, ArrayList<String>>{

        val result = hashMapOf<String, ArrayList<String>>()

        val c = database.rawQuery("SELECT * FROM sqlite_master WHERE type='table' ORDER BY name", null)

        if (c.moveToFirst()) {
            do {

                val nameIndex = c.getColumnIndex("name")

                val tableName = c.getString(nameIndex)

                val cc = database.rawQuery("PRAGMA table_info('$tableName')", null)

                val columns = arrayListOf<String>()

                if (cc.moveToFirst()) {
                    do {

                        val columnNameIndex = cc.getColumnIndex("name")

                        val columnName = cc.getString(columnNameIndex)

                        columns.add(columnName)

                    } while (cc.moveToNext())
                }

                cc.close()

                result[tableName] = columns

            }while (c.moveToNext())
        }

        c.close()

        return result

    }

    private fun addEntity(entity:STMEntityDescription){

        if (entity.abstract) return

        STMFunctions.debugLog("STMSQLiteSchema","add entity ${entity.entityName}")
        val columns = ArrayList(builtInAttributes)
        val tableName = STMFunctions.removePrefixFromEntityName(entity.entityName)
        val tableExists = isTableExists(tableName, database)
        if (!tableExists){
            migrationSuccessful = migrationSuccessful && executeDDL(createTableDDL(tableName))
        }

        val propertiesColumns = processPropertiesForEntity(entity, tableName)

        columns.addAll(propertiesColumns)
        if (columns.isEmpty()){

            columnsDictionary!!.remove(tableName)

        }

        columnsDictionary!![tableName] = columns

    }

    private fun executeDDL(ddls:ArrayList<String>):Boolean{

        if (ddls.isEmpty()) return true
        try {
            ddls
                    .filterNot { it.isEmpty() }
                    .forEach { database.execSQL(it) }
        }catch (e: SQLException){

            return false

        }

        return true

    }

    private fun createTableDDL(tableName:String):ArrayList<String>{

        val builtInColumns = arrayListOf<String>()

        builtInColumns.add(columnDDL(STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY, STMConstants.SQLiteText, "PRIMARY KEY"))
        builtInColumns.add(columnDDL(STMConstants.STMPersistingKeyCreationTimestamp, STMConstants.SQLiteText, STMConstants.SQLiteDefaultNow))
        builtInColumns.add(columnDDL(STMConstants.STMPersistingKeyVersion, STMConstants.SQLiteText, null))
        builtInColumns.add(columnDDL(STMConstants.STMPersistingOptionLts, STMConstants.SQLiteText, "DEFAULT('')"))
        builtInColumns.add(columnDDL(STMConstants.STMPersistingKeyPhantom, STMConstants.SQLiteInt, null))

        val clauses = arrayListOf<String>()

        clauses.add("CREATE TABLE IF NOT EXISTS [$tableName] (${builtInColumns.joinToString(", ")})")
        clauses.add(createIndexDDL(tableName, STMConstants.STMPersistingKeyPhantom))
        clauses.add(createIndexDDL(tableName, STMConstants.STMPersistingOptionLts))
        clauses.add(createIndexDDL(tableName, STMConstants.STMPersistingKeyVersion))

        val whenUpdated = "OLD.${STMConstants.STMPersistingKeyVersion} > OLD.${STMConstants.STMPersistingOptionLts}"
        val abortChanges = "SELECT RAISE(ABORT, 'ignored') WHERE OLD.${STMConstants.STMPersistingKeyVersion} <> NEW.${STMConstants.STMPersistingOptionLts}"

        clauses.add(createTriggerDDL("check_lts", "BEFORE UPDATE OF " + STMConstants.STMPersistingOptionLts, tableName, abortChanges, whenUpdated))

        val ignoreRemoved = "SELECT RAISE(IGNORE) FROM RecordStatus WHERE isRemoved = 1 AND objectXid = NEW.${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} LIMIT 1"

        clauses.add(createTriggerDDL("isRemoved", STMConstants.SQLiteBeforeInsert, tableName, ignoreRemoved, null))

        return clauses

    }

    private fun isTableExists(tableName: String, db:SQLiteDatabase): Boolean {
        var isExist = false
        val cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '$tableName'", null)
        if (cursor != null) {
            if (cursor.count > 0) {
                isExist = true
            }
            cursor.close()
        }
        return isExist
    }

    private fun processPropertiesForEntity(entity:STMEntityDescription, tableName:String):ArrayList<String>{

        val columns = arrayListOf<String>()
        var columnAttributes = entity.attributesByName.values
        columnAttributes = ArrayList(
                columnAttributes.filter {

                    return@filter !ignoredAttributes.contains(it.attributeName)

                }
        )

        val addedColumns = addColumns(columnAttributes, tableName)
        columns.addAll(addedColumns)
        val relationships = entity.relationshipsByName.values
        val addedRelationships = addRelationships(ArrayList(relationships), tableName)

        columns.addAll(addedRelationships)

        return columns

    }

    private fun addColumns(columnAttributes:ArrayList<STMAttributeDescription>, tableName:String):ArrayList<String>{

        val columns = arrayListOf<String>()
        for (attribute in columnAttributes){

            columns.add((attribute.attributeName))
            migrationSuccessful = migrationSuccessful && executeDDL(addAttributeDDL(attribute, tableName))
        }

        return columns

    }

    private fun addAttributeDDL(attribute:STMAttributeDescription, tableName:String):ArrayList<String>{

        val clauses = arrayListOf<String>()
        val columnName = attribute.attributeName
        val dataType = sqliteTypeForAttributeType(attribute.attributeType)
        val columnDDL = columnDDL(columnName, dataType, null)
        clauses.add("ALTER TABLE $tableName ADD COLUMN $columnDDL")
        if (attribute.indexed) {

            clauses.add(createIndexDDL(tableName, columnName))

        }

        return clauses

    }

    private fun addRelationships(relationships:ArrayList<STMRelationshipDescription>, tableName:String):ArrayList<String>{

        val columns = arrayListOf<String>()
        for (relationship in relationships){

            if (relationship.isToMany){

                val ddl = addToManyRelationshipDDL(relationship, tableName)

                if (ddl != null){

                    migrationSuccessful = migrationSuccessful && executeDDL(ddl)

                }

                continue

            }

            columns.add(relationship.relationshipName + STMConstants.RELATIONSHIP_SUFFIX)
            migrationSuccessful = migrationSuccessful && executeDDL(addRelationshipDDL(relationship, tableName)!!)

        }

        return columns

    }

    private fun sqliteTypeForAttributeType(attributeType:String):String{

        return when (attributeType) {
            "String", "Date", "Undefined", "Binary", "Transformable" -> STMConstants.SQLiteText
            "Integer 64", "Boolean", "Object ID", "Integer 16", "Integer 32" -> STMConstants.SQLiteInt
            "Decimal", "Float", "Double" -> STMConstants.SQLiteNumber
            else -> STMConstants.SQLiteText
        }

    }

    private fun addToManyRelationshipDDL(relationship:STMRelationshipDescription, tableName:String):ArrayList<String>{

        if (!relationship.isToMany){

            STMFunctions.debugLog("STMSQLLiteSchema","attempt to add non-to-many relationship with addToManyRelationshipDDL")
            return arrayListOf()

        }

        if (relationship.deleteRule != "Cascade") return arrayListOf()

        val name = relationship.relationshipName
        val childTableName = STMFunctions.removePrefixFromEntityName(relationship.destinationEntityName)
        val fkColumn = relationship.inverseRelationshipName!! + STMConstants.RELATIONSHIP_SUFFIX
        val deleteChildren = "DELETE FROM $childTableName WHERE $fkColumn = OLD.${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY}"
        return arrayListOf(createTriggerDDL(STMConstants.CASCADE_TRIGGER_PREFIX + name, STMConstants.SQLiteBeforeDelete, tableName, deleteChildren, null))

    }

    private fun createTriggerDDL(name:String, event:String, tableName:String, body:String, whenn:String?):String{

        val _when = if (whenn != null) "WHEN " + whenn else ""
        val formats = arrayListOf(
                "CREATE TRIGGER IF NOT EXISTS ${tableName}_$name",
                "$event ON [$tableName] FOR EACH ROW $_when",
                "BEGIN $body; END"
        )
        return formats.joinToString(" ")

    }

    private fun addRelationshipDDL(relationship:STMRelationshipDescription, tableName:String):ArrayList<String>{

        if (relationship.isToMany){

            STMFunctions.debugLog("STMSQLLiteSchema","attempt to add non-to-one relationship with addRelationshipDDL")
            return arrayListOf()

        }

        val clauses = arrayListOf<String>()
        val columnName = relationship.relationshipName + STMConstants.RELATIONSHIP_SUFFIX
        val parentName = STMFunctions.removePrefixFromEntityName(relationship.destinationEntityName)
        val constraints = "REFERENCES $parentName ON DELETE SET NULL"
        val columnDDL = columnDDL(columnName, STMConstants.SQLiteText, constraints)
        clauses.add("ALTER TABLE [$tableName] ADD COLUMN $columnDDL")
        clauses.add(createIndexDDL(tableName, columnName))
        val phantomFields = "INSERT INTO [$parentName] (${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY}, ${STMConstants.STMPersistingKeyPhantom}, ${STMConstants.STMPersistingOptionLts}, ${STMConstants.STMPersistingKeyVersion})"
        val phantomData = "SELECT NEW.$columnName, 1, null, null"
        val phantomSource = "WHERE NOT EXISTS (SELECT * FROM $parentName WHERE ${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} = NEW.$columnName)"
        val columnNotNull = "NEW.$columnName is not null"
        val createPhantom = arrayListOf(phantomFields, phantomData, phantomSource).joinToString(" ")
        clauses.add(createTriggerDDL("phantom_" + columnName, STMConstants.SQLiteBeforeInsert, tableName, createPhantom, columnNotNull))
        clauses.add(createTriggerDDL("phantom_update_" + columnName, "BEFORE UPDATE OF " + columnName, tableName, createPhantom, columnNotNull))

        return clauses

    }

    private fun columnDDL(columnName:String, datatype:String?, constraints:String?):String{

        val clauses = arrayListOf<String>()
        clauses.add(quoted(columnName))
        if (datatype != null){

            clauses.add(datatype)

        }
        if (constraints != null) {

            clauses.add(constraints)

        }

        return clauses.joinToString(" ")

    }

    private fun quoted(aString:String):String{

        return "[$aString]"

    }

    private fun createIndexDDL(tableName:String, columnName:String):String{

        return "CREATE INDEX IF NOT EXISTS ${tableName}_$columnName on $tableName ($columnName);"

    }

    private fun deleteEntity(entity:STMEntityDescription){

        STMFunctions.debugLog("STMSQLLiteSchema", "delete entity ${entity.entityName}")
        val tableName = STMFunctions.removePrefixFromEntityName(entity.entityName)
        val result = executeDDL(arrayListOf(dropTable(tableName)))

        if (result){

            columnsDictionary!!.remove(tableName)

        }

        migrationSuccessful = migrationSuccessful && result

    }

    private fun dropTable(tableName:String):String{

        return "DROP TABLE IF EXISTS $tableName"

    }

    private fun recreateEntityWithName(entityName:String){

        STMFunctions.debugLog("STMSQLiteSchema","recreateEntityWithName: $entityName")
        val tableName = STMFunctions.removePrefixFromEntityName(entityName)
        if (recreatedTables.contains(tableName)) return
        val entity = modelMapping!!.destinationModel.entitiesByName[entityName]
        deleteEntity(entity!!)
        addEntity(entity)
        tablesToReload.add(tableName)
        recreatedTables = tablesToReload

    }

    private fun deleteToManyRelationshipDDL(relationship:STMRelationshipDescription, tableName:String):String?{

        if (!relationship.isToMany){
            STMFunctions.debugLog("STMSQLiteSchema","attempt to delete non-to-many relationship with deleteToManyRelationshipDDL")
            return null
        }

        if (relationship.deleteRule != "Cascade") return null

        val name = STMConstants.CASCADE_TRIGGER_PREFIX + relationship.relationshipName

        return "DROP TRIGGER IF EXISTS ${tableName}_$name"

    }

    private fun addPropertiesArray(propertiesArray:ArrayList<STMPropertyDescription>, entityName:String){

        if (propertiesArray.count() == 0) return

        val tableName = STMFunctions.removePrefixFromEntityName(entityName)
        if (recreatedTables.contains(tableName)) return
        var columns = columnsDictionary?.get(tableName)

        if (columns == null) columns = arrayListOf()

        TODO("not implemented")
//        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"class == %@", [NSAttributeDescription class]];
//        NSArray <NSAttributeDescription *> *attributes = (NSArray <NSAttributeDescription *> *)[propertiesArray filteredArrayUsingPredicate:predicate];
//        if (attributes.count) {
//            NSLog(@"%@ add atributes: %@", entityName, [attributes valueForKeyPath:@"name"]);
//            [columns addObjectsFromArray:[self addColumns:attributes toTable:tableName]];
//            [self.tablesToReload addObject:tableName];
//        }
//        predicate = [NSPredicate predicateWithFormat:@"class == %@", [NSRelationshipDescription class]];
//        NSArray <NSRelationshipDescription *> *relationships = (NSArray <NSRelationshipDescription *> *)[propertiesArray filteredArrayUsingPredicate:predicate];
//        if (relationships.count) {
//            NSLog(@"%@ add relationships: %@", entityName, [relationships valueForKeyPath:@"name"]);
//            [columns addObjectsFromArray:[self addRelationships:relationships toTable:tableName]];
//            NSPredicate *predicate = [NSPredicate predicateWithFormat:@"toMany != YES"];
//            relationships = [relationships filteredArrayUsingPredicate:predicate];
//            if (relationships.count) {
//                [self.tablesToReload addObject:tableName];
//            }
//        }
//        self.columnsDictionary[tableName] = columns;

    }

    private fun fillRecreatedTablesWithFantom(){

        for (tableName in recreatedTables){

            val entityName = STMFunctions.addPrefixToEntityName(tableName)
            val entity = modelMapping!!.destinationModel.entitiesByName[entityName]
            fillWithFantoms(entity!!)

        }
    }

    private fun fillWithFantoms(entity:STMEntityDescription){

        STMFunctions.debugLog("STMSQLiteSchema", "fillWithFantoms: ${entity.entityName}")

        val tableName = STMFunctions.removePrefixFromEntityName(entity.entityName)
        var relationships = entity.relationshipsByName.values

        relationships = ArrayList(relationships.filter {

            return@filter it.isToMany && !modelMapping!!.destinationModel.entitiesByName[it.destinationEntityName]!!.relationshipsByName[it.inverseRelationshipName]!!.isToMany

        })

        relationships.forEach{

            STMFunctions.debugLog("STMSQLiteSchema","${entity.entityName} fill with fantoms")
            val toOneRelName = modelMapping!!.destinationModel.entitiesByName[it.destinationEntityName]!!.relationshipsByName[it.inverseRelationshipName]!!.relationshipName
            val toManyRelTableName = STMFunctions.removePrefixFromEntityName(it.destinationEntityName)
            val insertFantomsDDL = "INSERT INTO $tableName (id, isFantom, deviceCts) SELECT DISTINCT $toOneRelName, 1, null FROM $toManyRelTableName"
            migrationSuccessful = migrationSuccessful && executeDDL(arrayListOf(insertFantomsDDL))

        }

    }

    private fun eTagReseting(){

        if (tablesToReload.count() == 0){

            return

        }

        val questionMarks = arrayListOf<String>()

        tablesToReload.forEach{

            questionMarks.add("?")

        }

        val formatString = questionMarks.joinToString(",")

        val cv = ContentValues()

        cv.put("eTag", "*")

        try {

            database.update("ClientEntity", cv, "[name] IN ($formatString)", tablesToReload.toArray(arrayOf()))

        }catch(e:Exception) {

            migrationSuccessful = false

        }

    }

}