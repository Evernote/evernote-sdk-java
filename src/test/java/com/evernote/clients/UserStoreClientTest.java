/*
 * Copyright 2013 Evernote Corporation
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.evernote.clients;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.evernote.edam.userstore.UserStore;

public class UserStoreClientTest {

  final Set<String> IGNORE_METHODS = new HashSet<String>(Arrays.asList(
      "getOutputProtocol", "getInputProtocol"));

  @Test
  public void testWrappedMethods() {
    Method[] originalMethods = UserStore.Client.class.getDeclaredMethods();
    Method[] wrappedMethods = UserStoreClient.class.getDeclaredMethods();

    Set<String> originalMethodNames = new HashSet<String>();
    for (Method m : originalMethods) {
      if (!m.getName().matches("^(send_|recv_).*") && !IGNORE_METHODS.contains(m.getName())) {
        originalMethodNames.add(m.getName());
      }
    }

    for (Method m : wrappedMethods) {
      if (originalMethodNames.contains(m.getName())) {
        originalMethodNames.remove(m.getName());
      }
    }

    if (!originalMethodNames.isEmpty()) {
      fail("Following methods are not implemented: "
          + originalMethodNames.toString());
    }
  }

}
