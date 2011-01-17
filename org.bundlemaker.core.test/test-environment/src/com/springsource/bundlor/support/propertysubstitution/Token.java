/*
 * Copyright 2008-2009 SpringSource
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

package com.springsource.bundlor.support.propertysubstitution;

/**
 * Represents a token lex'd from a version expansion string
 * 
 * @author Andy Clement
 */
final class Token {

    TokenKind kind;

    int start;

    int end;

    public Token(TokenKind kind, int start, int end) {
        this.kind = kind;
        this.start = start;
        this.end = end;
    }

    // The kinds of token that can be lex'd from an version expansion string
    static enum TokenKind {
        DOT, EQUALS, COMMA, WORD, NUMBER, PLUSNUMBER, NEGATIVENUMBER, STARTINCLUSIVE, ENDINCLUSIVE, STARTEXCLUSIVE, ENDEXCLUSIVE;
    }
}