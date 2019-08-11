/////////////////////////////////////////////////////////////////////////
// Project Web Services
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.k2_ws.DECISION;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.user.WebServiceUserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.k2_ws.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.common.SharedGlobals.*;

import javax.xml.bind.Unmarshaller;
import com.google.common.collect.Multimap;
import com.google.common.base.CaseFormat;
import com.google.common.collect.Sets;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import com.sapiens.des.engine.model.FlowResult;
import com.sapiens.des.engine.model.IoFactType;
import com.sapiens.des.engine.model.IoGroup;
import com.sapiens.des.engine.model.IoGroupInstance;
import com.sapiens.des.engine.model.KeyValue;
import com.sapiens.des.execution.model.ExecutionInput;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import org.apache.commons.lang3.StringUtils;


@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends WebServiceUserCode {


	@out(name = "xml", type = Object.class, desc = "")
	public static Object getAllLuDecisionInfo(String luType, String instanceId) throws Exception {
		ExecutionInput inputs = new ExecutionInput();
		
		boolean successfullyExecuted;
		try {
			successfullyExecuted = DBExecute("fabric", "get " + luType + "." + instanceId, null);
			
			if (successfullyExecuted) {
				LUType lut = LUTypeFactoryImpl.getInstance().getTypeByName(luType.toString());
				LudbObject root  = lut.getRootObject();
				String rootTable = getRootDecisionTableName(luType);
				LudbObject table = lut.ludbObjects.get(rootTable);
				IoGroup rootNode = (IoGroup)fillLuChildTables(rootTable, null, table, "DECISION_ROOT", Sets.<String> newHashSet(), String.valueOf(instanceId));
				inputs.setIoGroup(rootNode);
			}
		
			JAXBContext context = JAXBContext.newInstance(ExecutionInput.class);
			Marshaller marshaller = context.createMarshaller();
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(inputs, stringWriter);
			String xml = stringWriter.toString();
			return xml;
		
		} catch (SQLException e) {
			String message = e.getMessage();
			reportUserMessage(message);
			return null;
		}
	}


	@out(name = "response", type = String.class, desc = "")
	public static String onDecisionCompletion(String executionResult, String luType, String instanceId) throws Exception {
		return null;
	}


	@out(name = "response", type = String.class, desc = "")
	public static String onFlowCompletion(String executionResult, String luType, String instanceId) throws Exception {
		return null;
	}

	
	

	
}
