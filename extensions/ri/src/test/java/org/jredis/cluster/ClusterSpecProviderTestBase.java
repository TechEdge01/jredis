/*
 *   Copyright 2009-2010 Joubin Houshyar
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *    
 *   http://www.apache.org/licenses/LICENSE-2.0
 *    
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.jredis.cluster;

import org.jredis.connector.ConnectionSpec;
import org.jredis.ri.alphazero.support.Log;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

/**
 * @author  joubin (alphazero@sensesay.net)
 * @date    Mar 24, 2010
 */
//TODO: look into the data provider attrib of the annotation ...
@Test(suiteName="extensions-cluster-specs-2")
abstract 
public class ClusterSpecProviderTestBase extends RefImplTestSuiteBase<ClusterSpec> {

	// ------------------------------------------------------------------------
	// Extension point
	// ------------------------------------------------------------------------
	
    protected abstract ClusterNodeSpec newNodeSpec (ConnectionSpec connectionSpec) ;

	// ------------------------------------------------------------------------
	// Specification Interface tested
	// ------------------------------------------------------------------------
	
	protected final Class<?> getSpecificationClass () {
		return ClusterSpec.class;
	}

	// ------------------------------------------------------------------------
	// Test general contract of SPECS for Cluster and its Nodes
	// ------------------------------------------------------------------------
	
	@Test
	public void testGetType() {
		Log.log("Testing ClusterSpec.getType()");
		assertNotNull(provider.getType(), "getType() must never return null");
	}
	
	@Test
	public void testSetType() {
		Log.log("Testing ClusterSpec.setType()");
		
		// Note: don't use provider instance as we are changing various settings here
		// and don't want to break assumptions down the hierarchy chain.

		ClusterType clusterType = null;
		ClusterSpec clusterSpec = newProviderInstance();
		ClusterType prevType = clusterSpec.getType();
		assertNotNull(prevType, "getType() must never return null");
		// just pick something else
		for(ClusterType type : ClusterType.values()){
			if(type != prevType){
				clusterType = type;
				break;
			}
		}
		assertNotNull(clusterType, "[BUG] why couldn't we find another different type?");
		
		// use the setter and test various requirements.
		ClusterSpec chainedRes = clusterSpec.setType(clusterType);
		testChainedResult(chainedRes, clusterSpec);
//		assertNotNull(chainedRes, "fluent interface setters must return non null values");
//		assertEquals(chainedRes, aClusterSpec, "setter result must be the same reference as the original");
		
		assertEquals(clusterSpec.getType(), clusterType, "getType() result must match the ref used for setType()");
	}
	
	@Test
	public void testAddNodeSpec() {
		Log.log("Testing ClusterSpec.addNodeSpec()");
		ClusterSpec clusterSpec = newProviderInstance();
		
		try {
			ClusterNodeSpec nodeSpec = newNodeSpec(data.connSpecs[0]);
			ClusterSpec res = clusterSpec.addNode(nodeSpec);
			testChainedResult(res, clusterSpec);
		}
		catch (Exception e){ fail("when adding a unique spec", e); }
		
		try {
			ClusterNodeSpec nodeSpec = newNodeSpec(data.connSpecs[1]);
			clusterSpec.addNode(nodeSpec);
		}
		catch (Exception e){ fail("when adding a unique spec", e); }
		
		// now lets raise some errors
		boolean didRaiseError;
		
		// should not allow adding of duplicate ClusterNodeSpecs
		didRaiseError = false;
		
		assertTrue(clusterSpec.addNode(newNodeSpec(data.defRedisDb10Port7777ConnSpec)) == clusterSpec, "add of unique spec should be possible and must return the clusterSpec instance");
		try {
			assertTrue(clusterSpec.addNode(newNodeSpec(data.defRedisDb10Port7777ConnSpec_dup)) == clusterSpec, "add of duplicate spec is expected to raise a runtime exception");
		}
		catch (IllegalArgumentException e){
			didRaiseError = true;
		}
		if(!didRaiseError) fail("Expecting an IllegalArgumentException raised for duplicate ClusterNodeSpec to add()");
		
		// should not allow adding of null specs
		didRaiseError = false;
		ClusterNodeSpec nullRef = null;
		try {
			assertTrue(clusterSpec.addNode(nullRef) == clusterSpec, "add of null spec is expected to raise a runtime exception");
		}
		catch (IllegalArgumentException e){
			didRaiseError = true;
		}
		if(!didRaiseError) fail("Expecting an IllegalArgumentException raised for null input arg to add()");
	}
	
	// ------------------------------------------------------------------------
	// helper methods
	// ------------------------------------------------------------------------
	
	private final void testChainedResult (ClusterSpec res, ClusterSpec expected) {
		assertNotNull(res, "fluent interface setters must return non null values");
		assertEquals(res, expected, "setter result must be the same reference as the original");
	}
}
