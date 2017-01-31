/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jini.lookup.ui.factory;

import java.awt.Panel;

/**
 * UI factory for an AWT <CODE>Panel</CODE>.
 *
 * <P>If the UI generated by the method declared in this interface implements
 * <CODE>javax.accessibility.Accessible</CODE> and supports the Java Accessibility
 * API, an <CODE>AccessibleUI</CODE> attribute
 * should be placed in the <CODE>UIDescriptor</CODE>'s <CODE>attributes</CODE> set.
 *
 * <P>A <CODE>PreferredTitle</CODE> may be placed in the attributes set to indicate
 * a preferred title of a pop-up window containing this component.
 *
 * @author Bill Venners
 */
public interface PanelFactory extends java.io.Serializable {

    /**
     * Convenience constant to use in the <CODE>toolkit</CODE>
     * field of <CODE>UIDescriptor</CODE>s that contain a
     * <CODE>PanelFactory</CODE>.
     * The value of this constant is <CODE>"java.awt"</CODE>.
     */
    String TOOLKIT = "java.awt";

    /**
     * Convenience constant to use in the <CODE>UIFactoryTypes</CODE>
     * set in the <CODE>attributes</CODE> set of <CODE>UIDescriptor</CODE>s
     * that contain a <CODE>JPanelFactory</CODE>.
     * The value of this constant is <CODE>"net.jini.lookup.ui.factory.PanelFactory"</CODE>.
     */
    String TYPE_NAME = "net.jini.lookup.ui.factory.PanelFactory";

    /**
     * Returns a <CODE>Panel</CODE>.
     */
    Panel getPanel(Object roleObject);
}
