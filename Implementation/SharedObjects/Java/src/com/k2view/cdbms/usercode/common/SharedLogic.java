/////////////////////////////////////////////////////////////////////////
// Project Shared Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.common;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;

import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.shared.user.UserCode.*;
import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

import javax.xml.bind.Unmarshaller;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Multimap;
import java.util.stream.Collectors;
import javax.xml.bind.*;
import javax.xml.namespace.*;
import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import com.sapiens.des.engine.model.FlowResult;
import com.sapiens.des.engine.model.IoFactType;
import com.sapiens.des.engine.model.IoGroup;
import com.sapiens.des.engine.model.IoGroupInstance;
import com.sapiens.des.engine.model.KeyValue;
import com.sapiens.des.execution.model.ExecutionInput;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import com.google.common.base.*;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;


@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class SharedLogic {


	@category("DECISION")
	@out(name = "val", type = Object.class, desc = "")
	public static Object fillLuChildTables(String tableName, Object iogInstanceInput, Object tableInput, String parentTableName, Set<String> parentTableNames, String parentInstanceId) throws Exception {
		LudbObject table = (LudbObject)tableInput;
		IoGroupInstance iogInstance = (IoGroupInstance)iogInstanceInput;
		
		StringBuilder sqlQuery = new StringBuilder("select * from " + tableName);
		if(parentInstanceId != null) {
				
			String whereClause = " where " + parentTableName + "_ID = '" + String.valueOf(parentInstanceId) + "'"; //New!
			sqlQuery.append(whereClause);
		}
				
		Set<String> defensiveParentTableName = new HashSet<>(parentTableNames);
		defensiveParentTableName.add(tableName);
				
		ResultSetWrapper resultsWrapper = DBQuery("fabric", sqlQuery.toString(), null);
		
		List<Object[]> results = new ArrayList<>();
		resultsWrapper.forEach(result -> results.add(result));
		
		List<String> columnNames = resultsWrapper.getColumnsNames();
				
		IoGroup ioGroup = null;
				
		if(iogInstance == null){ 
			parentTableName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.replace("DECISION_", "")); 
			ioGroup = new IoGroup(parentTableName);
		}
		else {
			String tableNameUpperCamel = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, tableName.replace("DECISION_", ""));
			ioGroup = new IoGroup(tableNameUpperCamel);
			iogInstance.getIoGroups().add(ioGroup);
		}
		ioGroup.setIoGroupInstances(new ArrayList<>());
		
		for (Object[] result : results) {
			
			IoGroupInstance specificInstance = new IoGroupInstance();
			ioGroup.getIoGroupInstances().add(specificInstance);
			specificInstance.setIoFactTypes(new ArrayList<>());
			specificInstance.setIoGroups(new ArrayList<>());
			Set<KeyValue> executionKeyValues = Sets.newHashSet();
			for (int i = 0; i < columnNames.size(); i++) {
				
				String columnName = columnNames.get(i);
				Object columnValue = result[i];
				
				if(columnName.equalsIgnoreCase(tableName + "_ID")){
					
					KeyValue instanceNameAndId = new KeyValue();
					instanceNameAndId.setKey(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName));
					instanceNameAndId.setValue(String.valueOf(columnValue));
					executionKeyValues.add(instanceNameAndId);
					
					if (table.childObjects != null) {
						for (LudbObject tableChild : table.childObjects) {
							if (tableChild.ludbObjectName.startsWith("DECISION_")) {
				
								fillLuChildTables(tableChild.ludbObjectName,
										specificInstance, tableChild,
										tableName, defensiveParentTableName, String.valueOf(columnValue));
							}
						}
					}
				}  else {
						List<String> parentTablesAsColumn = parentTableNames.stream()
							.map(parentTableStr -> (parentTableStr + "_ID").toLowerCase())
							.collect(Collectors.toList());
			
						String msg = "Current table: " + tableName + "\ncurrent column: " + columnName;
						reportUserMessage(msg + "====================>" + String.join(", ", parentTablesAsColumn));
						if (parentTablesAsColumn.contains(columnName.toLowerCase())){ 
							KeyValue instanceNameAndId = new KeyValue();
							instanceNameAndId.setKey(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName));
							instanceNameAndId.setValue(String.valueOf(columnValue));
							executionKeyValues.add(instanceNameAndId);
						} else {
		
							IoFactType factType = new IoFactType();
							factType.setName(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, columnName));
							setFactTypeValue(factType, columnValue);
							specificInstance.getIoFactTypes().add(factType);
						}
				  }
			}
			specificInstance.setExecutionKeyValues(executionKeyValues);
		}
		
		return ioGroup;
	}


	@category("DECISION")
	@out(name = "rootTable", type = String.class, desc = "")
	public static String getRootDecisionTableName(String luName) throws Exception {
		ResultSetWrapper results = DBQuery("fabric", "pragma " + luName + ".table_list", null);
		Set<String> tableNames = new HashSet<>();
		for (Object[] objects : results) {
			tableNames.add(objects[0].toString());
		}
		
		return tableNames.stream()
				.filter(name -> name.startsWith("DECISION_ROOT"))
				.findFirst()
				.orElse(null);
	}


	@category("DECISION")
	public static void setFactTypeValue(Object factTypeInput, Object columnValue) throws Exception {
		IoFactType factType =(IoFactType)factTypeInput;
		
		if (columnValue != null){
			String value = columnValue.toString().trim();
			if (value.startsWith("{") && value.endsWith("}")) {
				value = value.replace("{", "").replace("}", "");
				String[] values = value.split(",");
				factType.setValuesAsString(Arrays.asList(values));
			} else {
				factType.setValue(columnValue);
			}
		}
	}


	@category("String")
	@out(name = "val", type = String.class, desc = "")
	public static String stringValsTogether() throws Exception {
		List<String> a = new ArrayList<>();
		
		String sql = "SELECT NAME FROM BANK";
		Object[] valuesArr = null;
		ResultSetWrapper rs = DBQuery("oracle_cloud", sql, valuesArr);
		
		for(Object[] row : rs) {
			a.add(row[0].toString());
		}
		
		rs.closeStmt();
		
		
		return "{"+ String.join(",", a) + "}";
	}

	
	
	

}
