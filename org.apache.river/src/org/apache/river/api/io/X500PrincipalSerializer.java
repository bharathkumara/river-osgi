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

package org.apache.river.api.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.util.Arrays;
import javax.security.auth.x500.X500Principal;
import org.apache.river.api.io.AtomicSerial.GetArg;

/**
 *
 * @author peter
 */
@Serializer(replaceObType = X500Principal.class)
@AtomicSerial
class X500PrincipalSerializer implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * By defining serial persistent fields, we don't need to use transient fields.
     * All fields can be final and this object becomes immutable.
     */
    private static final ObjectStreamField[] serialPersistentFields = 
    {
        new ObjectStreamField("encoded", byte[].class)
    };
    
    private final byte [] encoded;
    private final X500Principal principal;
    
    X500PrincipalSerializer(X500Principal principal){
        this.principal = principal;
        encoded = principal.getEncoded();
    }
    
    X500PrincipalSerializer(GetArg arg) throws IOException{
        this(new X500Principal(arg.get("encoded", new byte[0], byte[].class)));
    }
    
    Object readResolve() throws ObjectStreamException {
	if (principal != null) return principal;
        return new X500Principal(encoded);
    }
    
    /**
     * @serialData 
     * @param out
     * @throws IOException 
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
	ObjectOutputStream.PutField pf = out.putFields();
	pf.put("encoded", encoded);
	out.writeFields();
    }
    
    @Override
    public boolean equals(Object obj){
        if (!(obj instanceof X500PrincipalSerializer)) return false;
        if (obj == this) return true;
        X500PrincipalSerializer that = (X500PrincipalSerializer) obj;
        return Arrays.equals(encoded, that.encoded);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Arrays.hashCode(this.encoded);
        return hash;
    }
    
}
