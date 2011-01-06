/*
 * This file is part of d3.
 * 
 * d3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * d3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with d3.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2010 Guilhelm Savin
 */
package org.ri2c.d3.protocol.xmpp.stanzas;

import org.ri2c.d3.protocol.xml.XMLStanza;

/**
 * ErrorStanza is a set of error stanzas defined in Section 4.7.3 of rfc3920.
 * References in stanza descriptions are linked to this rfc.
 * 
 * @author Guilhelm Savin
 *
 */
public class ErrorStanza
	extends XMLStanza
{
	/**
	 * the entity has sent XML that cannot be processed;
     * this error MAY be used instead of the more specific XML-related
     * errors, such as <bad-namespace-prefix/>, <invalid-xml/>,
     * <restricted-xml/>, <unsupported-encoding/>, and
     * <xml-not-well-formed/>, although the more specific errors are
     * preferred.
	 */
	public static final ErrorStanza BAD_FORMAT = 
		new ErrorStanza( "bad-format", "" );
	
	/**
	 * the entity has sent a namespace prefix
     * that is unsupported, or has sent no namespace prefix on an element
     * that requires such a prefix (see XML Namespace Names and Prefixes
     * (Section 11.2)).
	 */
	public static final ErrorStanza BAD_NAMESPACE_PREFIX =
		new ErrorStanza( "bad-namespace-prefix", "" );
	
	/**
	 * the server is closing the active stream for this
     * entity because a new stream has been initiated that conflicts with
     * the existing stream.
	 */
	public static final ErrorStanza CONFLICT =
		new ErrorStanza( "conflict", "" );

	/**
	 * the entity has not generated any traffic
     * over the stream for some period of time (configurable according to
     * a local service policy).
	 */
	public static final ErrorStanza CONNECTION_TIMEOUT =
		new ErrorStanza( "connection-timeout", "" );

	/**
	 * the value of the 'to' attribute provided by the
     * initiating entity in the stream header corresponds to a hostname
     * that is no longer hosted by the server.
	 */
	public static final ErrorStanza HOST_GONE =
		new ErrorStanza( "host-gone", "" );

	/**
	 * the value of the 'to' attribute provided by the
     * initiating entity in the stream header does not correspond to a
     * hostname that is hosted by the server.
	 */
	public static final ErrorStanza HOST_UNKNOWN =
		new ErrorStanza( "host-unknown", "" );

	/**
	 * a stanza sent between two servers lacks
     * a 'to' or 'from' attribute (or the attribute has no value).
	 */
	public static final ErrorStanza IMPROPER_ADDRESSING =
		new ErrorStanza( "improper-addressing", "" );

	/**
	 * the server has experienced a
      misconfiguration or an otherwise-undefined internal error that
      prevents it from servicing the stream.
	 */
	public static final ErrorStanza INTERNAL_SERVER_ERROR =
		new ErrorStanza( "internal-server-error", "" );

	/**
	 * the JID or hostname provided in a 'from'
     * address does not match an authorized JID or validated domain
     * negotiated between servers via SASL or dialback, or between a
     * client and a server via authentication and resource binding.
	 */
	public static final ErrorStanza INVALID_FROM =
		new ErrorStanza( "invalid-from", "" );

	/**
	 * the stream ID or dialback ID is invalid or does
     * not match an ID previously provided.
	 */
	public static final ErrorStanza INVALID_ID =
		new ErrorStanza( "invalid-id", "" );

	/**
	 * the streams namespace name is something
     * other than "http://etherx.jabber.org/streams" or the dialback
     * namespace name is something other than "jabber:server:dialback"
     * (see XML Namespace Names and Prefixes (Section 11.2)).
	 */
	public static final ErrorStanza INVALID_NAMESPACE =
		new ErrorStanza( "invalid-namespace", "" );

	/**
	 * the entity has sent invalid XML over the stream
     * to a server that performs validation (see Validation (Section
     * 11.3)).
	 */
	public static final ErrorStanza INVALID_XML =
		new ErrorStanza( "invalid-xml", "" );

	/**
	 * the entity has attempted to send data before
     * the stream has been authenticated, or otherwise is not authorized
     * to perform an action related to stream negotiation; the receiving
     * entity MUST NOT process the offending stanza before sending the
     * stream error.
	 */
	public static final ErrorStanza NOT_AUTHORIZED =
		new ErrorStanza( "not-authorized", "" );

	/**
	 * the entity has violated some local service
     * policy; the server MAY choose to specify the policy in the <text/>
     * element or an application-specific condition element.
	 */
	public static final ErrorStanza POLICY_VIOLATION =
		new ErrorStanza( "policy-violation", "" );

	/**
	 * the server is unable to properly
     * connect to a remote entity that is required for authentication or
     * authorization.
	 */
	public static final ErrorStanza REMOTE_CONNECTION_FAILED =
		new ErrorStanza( "remote-connection-failed", "" );

	/**
	 * the server lacks the system resources
     * necessary to service the stream.
	 */
	public static final ErrorStanza RESOURCE_CONSTRAINT =
		new ErrorStanza( "resource-constraint", "" );

	/**
	 * the entity has attempted to send restricted
     * XML features such as a comment, processing instruction, DTD,
     * entity reference, or unescaped character (see Restrictions
     * (Section 11.1)).
	 */
	public static final ErrorStanza RESTRICTED_XML =
		new ErrorStanza( "restricted-xml", "" );

	/**
	 * the server will not provide service to the
     * initiating entity but is redirecting traffic to another host; the
     * server SHOULD specify the alternate hostname or IP address (which
     * MUST be a valid domain identifier) as the XML character data of
     * the <see-other-host/> element.
	 */
	public static final ErrorStanza SEE_OTHER_HOST =
		new ErrorStanza( "see-other-host", "" );

	/**
	 * the server is being shut down and all active
     * streams are being closed.
	 */
	public static final ErrorStanza SYSTEM_SHUTDOWN =
		new ErrorStanza( "system-shutdown", "" );

	/**
	 * the error condition is not one of those
     * defined by the other conditions in this list; this error condition
     * SHOULD be used only in conjunction with an application-specific
     * condition.
	 */
	public static final ErrorStanza UNDEFINED_CONDITION =
		new ErrorStanza( "undefined-condition", "" );

	/**
	 * the initiating entity has encoded the
     * stream in an encoding that is not supported by the server (see
     * Character Encoding (Section 11.5)).
	 */
	public static final ErrorStanza UNSUPPORTED_ENCODING =
		new ErrorStanza( "unsupported-encoding", "" );

	/**
	 * the initiating entity has sent a
     * first-level child of the stream that is not supported by the
     * server.
	 */
	public static final ErrorStanza UNSUPPORTED_STANZA_TYPE =
		new ErrorStanza( "unsupported-stanza-type", "" );

	/**
	 * the value of the 'version' attribute
     * provided by the initiating entity in the stream header specifies a
     * version of XMPP that is not supported by the server; the server
     * MAY specify the version(s) it supports in the <text/> element.
	 */
	public static final ErrorStanza UNSUPPORTED_VERSION =
		new ErrorStanza( "unsupported-version", "" );

	/**
	 * the initiating entity has sent XML that
     * is not well-formed as defined by [XML].
	 */
	public static final ErrorStanza XML_NOT_WELL_FORMED =
		new ErrorStanza( "xml-not-well-formed", "" );
	
	private ErrorStanza( String name, String desc )
	{
		super("stream:error");
		
		XMLStanza definedCondition = new XMLStanza(name);
		definedCondition.addAttribute( "xmlns", "urn:ietf:params:xml:ns:xmpp-streams" );
		
		XMLStanza text = new XMLStanza("text");
		text.addAttribute( "xmlns", "urn:ietf:params:xml:ns:xmpp-streams" );
		text.appendContent(desc);
		
		addChild(definedCondition);
		addChild(text);
	}
}
