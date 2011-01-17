/*
 * Copyright 2009 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.bundlor.support.contributors;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.springsource.bundlor.support.ArtifactAnalyzer;
import com.springsource.bundlor.support.partialmanifest.PartialManifest;

public class JspArtifactAnalyzer implements ArtifactAnalyzer {

    private static final String PACKAGE_SUFFIX = ".*";

    private static final String TYPE_SUFFIX = ".class";

    private final Pattern pattern = Pattern.compile("<%@ page.*import=\"(.*?)\".*%>");

    public void analyse(InputStream artifact, String artifactName, PartialManifest partialManifest) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(artifact));
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            for (Matcher matcher = pattern.matcher(line); matcher.find();) {
                processImports(matcher.group(1), partialManifest);
            }
        }
    }

    public boolean canAnalyse(String artifactName) {
        return artifactName.endsWith(".jsp");
    }

    private void processImports(String imports, PartialManifest partialManifest) {
        for (String importString : imports.split(",")) {
            String trimmedString = importString.trim();
            if (trimmedString.endsWith(TYPE_SUFFIX)) {
                processType(trimmedString, partialManifest);
            } else if (trimmedString.endsWith(PACKAGE_SUFFIX)) {
                processPackage(trimmedString, partialManifest);
            }
        }
    }

    private void processType(String importString, PartialManifest partialManifest) {
        String typeName = importString.substring(0, importString.length() - TYPE_SUFFIX.length());
        partialManifest.recordReferencedType(typeName);

    }

    private void processPackage(String importString, PartialManifest partialManifest) {
        String packageName = importString.substring(0, importString.length() - PACKAGE_SUFFIX.length());
        partialManifest.recordReferencedPackage(packageName);
    }

}
