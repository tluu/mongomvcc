// This file is part of MongoMVCC.
//
// Copyright (c) 2012 Fraunhofer IGD
//
// MongoMVCC is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// MongoMVCC is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with MongoMVCC. If not, see <http://www.gnu.org/licenses/>.

package de.fhg.igd.mongomvcc.impl;

import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import com.mongodb.DBCursor;

import de.fhg.igd.mongomvcc.VBranch;
import de.fhg.igd.mongomvcc.VCollection;
import de.fhg.igd.mongomvcc.VConstants;
import de.fhg.igd.mongomvcc.VCursor;
import de.fhg.igd.mongomvcc.VDatabase;
import de.fhg.igd.mongomvcc.VFactory;

/**
 * Abstract base class for all MongoDB MVCC database tests
 * @author Michel Kraemer
 */
public abstract class AbstractMongoDBVDatabaseTest {
	protected VDatabase _db;
	protected VBranch _master;
	protected static final VFactory _factory = new MongoDBVFactory();
	
	/**
	 * Before all unit tests run, make sure the database is clean
	 */
	@BeforeClass
	public static void setUpClass() {
		VDatabase db = _factory.createDatabase();
		db.connect("mvcctest");
		db.drop();
	}
	
	/**
	 * Setup test database
	 */
	@Before
	public void setUp() {
		_db = _factory.createDatabase();
		_db.connect("mvcctest");
		_master = _db.checkout(VConstants.MASTER);
	}
	
	/**
	 * Delete test database
	 */
	@After
	public void tearDown() {
		_db.drop();
	}
	
	/**
	 * Put a person into the database
	 * @param name the person's name
	 * @param age the person's age
	 * @return the person (after the put)
	 */
	protected Map<String, Object> putPerson(String name, int age) {
		VCollection persons = _master.getCollection("persons");
		assertNotNull(persons);
		Map<String, Object> peter = new HashMap<String, Object>();
		peter.put("name", name);
		peter.put("age", age);
		persons.insert(peter);
		assertNotNull(peter.get("uid"));
		return peter;
	}
	
	/**
	 * Extracts a {@link DBCursor} from a {@link VCursor}. Throws a
	 * {@link IllegalStateException} if the cursor could not be extracted.
	 * This method is useful if tests want to access the database
	 * driver's cursor directly.
	 * @param c the MongoMVCC cursor
	 * @return the delegate cursor
	 */
	protected DBCursor extractDBCursor(VCursor c) {
		try {
			Field f = MongoDBVCursor.class.getDeclaredField("_delegate");
			boolean accessible = f.isAccessible();
			f.setAccessible(true);
			try {
				return (DBCursor)f.get(c);
			} finally {
				f.setAccessible(accessible);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not extract " +
					"DBCursor from VCursor", e);
		}
	}
	
	/**
	 * Checks if the given {@link VCursor} has a filter attached to it
	 * @param c the cursor
	 * @return true if a filter is attached, false otherwise
	 */
	protected boolean hasAttachedFilter(VCursor c) {
		try {
			Field f = MongoDBVCursor.class.getDeclaredField("_filter");
			boolean accessible = f.isAccessible();
			f.setAccessible(true);
			try {
				return f.get(c) != null;
			} finally {
				f.setAccessible(accessible);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Could not extract " +
					"filter from VCursor", e);
		}
	}
}
