/////////////////////////////////////////////////////////////////////////
// LU Functions
/////////////////////////////////////////////////////////////////////////

package com.k2view.cdbms.usercode.lu.totalLoanFlow__1_0.Root;

import java.util.*;
import java.sql.*;
import java.math.*;
import java.io.*;

import com.k2view.cdbms.shared.*;
import com.k2view.cdbms.shared.Globals;
import com.k2view.cdbms.shared.user.UserCode;
import com.k2view.cdbms.sync.*;
import com.k2view.cdbms.lut.*;
import com.k2view.cdbms.shared.utils.UserCodeDescribe.*;
import com.k2view.cdbms.shared.logging.LogEntry.*;
import com.k2view.cdbms.func.oracle.OracleToDate;
import com.k2view.cdbms.func.oracle.OracleRownum;
import com.k2view.cdbms.usercode.lu.totalLoanFlow__1_0.*;

import static com.k2view.cdbms.shared.utils.UserCodeDescribe.FunctionType.*;
import static com.k2view.cdbms.shared.user.ProductFunctions.*;
import static com.k2view.cdbms.usercode.common.SharedLogic.*;
import static com.k2view.cdbms.usercode.lu.totalLoanFlow__1_0.Globals.*;

@SuppressWarnings({"unused", "DefaultAnnotationParam"})
public class Logic extends UserCode {


	@type(RootFunction)
	@out(name = "DECISION_ROOT_id", type = String.class, desc = "")
	@out(name = "DECISION_BORROWER_id", type = String.class, desc = "")
	public static void fnPop_DECISION_BORROWER(String DECISION_ROOT_id) throws Exception {
		// auto generated - 13/02/2018 12:04:26
	}


	@type(RootFunction)
	@out(name = "DECISION_BORROWER_id", type = String.class, desc = "")
	@out(name = "LOAN_TYPE", type = String.class, desc = "")
	@out(name = "LOAN_AMOUNT", type = Double.class, desc = "")
	public static void fnPop_DECISION_LOAN(String DECISION_BORROWER_id) throws Exception {
		// auto generated - 13/02/2018 12:04:26
	}


	@type(RootFunction)
	@out(name = "DECISION_ROOT_id", type = String.class, desc = "")
	public static void fnPop_DECISION_ROOT(String DECISION_ROOT_id) throws Exception {
		// auto generated - 13/02/2018 12:04:26
	}

	
	
	
	
}
