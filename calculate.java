package com.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.*;
import java.sql.DriverManager;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Servlet implementation class calculate
 */
@WebServlet("/calculate")
public class calculate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public calculate() {
		super();
		// TODO Auto-generated constructor stub

	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// response.getWriter().append("Served at: ").append(request.getContextPath());

		String jsonString = "";
		PrintWriter writer = response.getWriter();

		// link to database
		final String USER = "postgres";
		final String PASS = "demo";
		Connection dbConnect = null;
		Statement dbStatement = null;

		try {
			Class.forName("org.postgresql.Driver");
			dbConnect = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", USER, PASS);
			System.out.println("Opened database successfully");

			// query
			System.out.println("Creating statement...");
			dbStatement = dbConnect.createStatement();
			String sql = "SELECT * FROM calculate_record";
			ResultSet rs = dbStatement.executeQuery(sql);
			System.out.println("Query success");
			while (rs.next()) {

				// Retrieve by column name
				String currency = rs.getString("currency");
				float rate = rs.getFloat("rate");
				float price = rs.getFloat("price");
				float discount = rs.getFloat("discount");
				float result = rs.getFloat("result");
				Timestamp record_time = rs.getTimestamp("record_time");

				// Convert Timestamp
				TimeZone timeZone = TimeZone.getTimeZone("GMT :08:00");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeZone(timeZone);
				calendar.setTime(record_time);
				SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				SDF.setTimeZone(timeZone);
				String record_time_s = SDF.format(calendar.getTime());

				// Display values
				DecimalFormat df = new DecimalFormat("0.00");
				System.out.println(currency);
				System.out.println(df.format(rate));
				System.out.println(df.format(price));
				System.out.println(df.format(discount));
				System.out.println(df.format(result));
				System.out.println(record_time_s);

				// write response
				jsonString = new JSONObject().put("result", "success").put("message", "")
						.put("data",
								new JSONObject().put("currency", currency).put("rate", df.format(rate))
										.put("price", df.format(price)).put("discount", df.format(discount))
										.put("result", df.format(result)).put("record_time", record_time_s))
						.toString();
				System.out.println(jsonString);
				writer.write(jsonString);
			}
			rs.close();
		} catch (SQLException se) {
			se.printStackTrace();

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		} finally {
			try {
				if (dbStatement != null)
					dbConnect.close();
			} catch (SQLException se) {
			}
			try {
				if (dbConnect != null)
					dbConnect.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// init
		Float price;
		Float discount;
		String currency;
		Float usdTwd, usdJpy;
		String usdTwdTime, useJpyTime;

		// catch the data by json
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;

		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				stringBuffer.append(line);
		} catch (Exception e) {
			/* report an error */ }

		try {
			JSONObject jsonObject = new JSONObject(stringBuffer.toString());
			price = jsonObject.getFloat("price");
			discount = jsonObject.getFloat("discount");
			currency = jsonObject.getString("currency");
			if (price < 0.0 || discount < 0.0 || price - discount < 0.0)
				return;

		} catch (JSONException e) {
			// crash and burn
			throw new IOException("Error parsing JSON request string");
		}
		try {
			// access the website and parsing JSON
			URL url = new URL("https://tw.rter.info/capi.php");
			URLConnection con = url.openConnection();
			InputStream inputStream = con.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			line = null;
			stringBuffer = new StringBuffer();
			while ((line = br.readLine()) != null) {
				stringBuffer.append(line);
			}
			JSONObject jsonObjectWeb = new JSONObject(stringBuffer.toString());
			JSONObject usdTwdSet = jsonObjectWeb.getJSONObject("USDTWD");
			JSONObject usdJpySet = jsonObjectWeb.getJSONObject("USDJPY");
			usdTwd = usdTwdSet.getFloat("Exrate");
			usdJpy = usdJpySet.getFloat("Exrate");
			usdTwdTime = usdTwdSet.getString("UTC");
			useJpyTime = usdJpySet.getString("UTC");
		} catch (Exception e) {
			throw new IOException("Error to access the website");
		}

		// calculate
		Float rate = null;
		Float result = null;
		if (currency.equals("USD")) {
			rate = usdTwd;
			result = price * usdTwd;
		} else if (currency.equals("TWD")) {
			rate = (float) 1.0;
			result = (float) Math.round(price - discount);
		} else if (currency.equals("JPY")) {
			rate = (float) usdTwd / usdJpy;
			result = price / usdJpy * usdTwd;
		} else {
			throw new IOException("Error to comvert money");
		}

		// write response
		DecimalFormat df = new DecimalFormat("0.00");
		PrintWriter writer = response.getWriter();
		String jsonString = new JSONObject().put("result", Float.parseFloat(df.format(result)))
				.put("discount", Float.parseFloat(df.format(discount))).put("price", Float.parseFloat(df.format(price)))
				.put("rate", Float.parseFloat(df.format(rate))).put("currency", currency).toString();
		System.out.println(jsonString);
		writer.write(jsonString);

		// insert to SQL
		final String USER = "postgres";
		final String PASS = "demo";
		// establish connection
		// link to database
		Connection dbConnect = null;
		Statement dbStatement = null;

		try {
			Class.forName("org.postgresql.Driver");
			dbConnect = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", USER, PASS);
			// query
			System.out.println("Creating statement...");
			dbStatement = dbConnect.createStatement();
			String sql = "INSERT INTO calculate_record VALUES ( \'" + currency + "\' , " + df.format(rate) + " , "
					+ df.format(price) + " , " + df.format(discount) + " , " + df.format(result) + ")";
			System.out.println(sql);
			dbStatement.executeUpdate(sql);
			System.out.println("Query success");
			dbConnect.close();
			dbStatement.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		} finally {
			try {
				if (dbStatement != null)
					dbConnect.close();
			} catch (SQLException se) {
			}
			try {
				if (dbConnect != null)
					dbConnect.close();
			} catch (SQLException se) {
				se.printStackTrace();
			}
		}
		// doGet(request, response);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
