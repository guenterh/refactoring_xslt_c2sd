<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                exclude-result-prefixes="fn"

>
    <!--xmlns:fn="http://www.w3.org/2005/xpath-functions"> -->

    <xsl:output method="xml"
                encoding="UTF-8"
                indent="yes"
                omit-xml-declaration="yes"
    />

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            Dieses Skript ist die zweite Stufe (step2) der Verarbeitung im Document-Processing zur Aufbereitung
            der Daten vor der eigentlich Indexierung.
            Siehe Kurzbeschreibung im Step1, Dokumentation im wiki, Issues, etc.

            ************************
            www.swissbib.org
            guenter.hipler@unibas.ch
            oliver.schihin@unibas.ch
            matthias.edel@unibas.ch
            ************************

            ****************************
            07.02.2013 : Guenter : integration of complete holdings structure into the index as stored field
            ****************************
            09.08.2013 : Oliver : Beginn Anpassungen neues CBS
            ****************************
            24.01.2019 : Matthias: Beginn hinzufügen der hierarchischen Library-Facet
            ****************************
        </desc>
    </doc>

    <xsl:param name="holdingsStructure" select="''"/>

    <!--=================
        CALLING TEMPLATES
        =================-->
    <xsl:template match="/">
        <doc>

            <xsl:call-template name="id_type">
                <xsl:with-param name="fragment" select="record"/>
            </xsl:call-template>
            <xsl:call-template name="lang_country">
                <xsl:with-param name="fragment" select="record" />
            </xsl:call-template>

            <xsl:call-template name="fulltext">
                <xsl:with-param name="fragment" select="record" />
            </xsl:call-template>

      </doc>
    </xsl:template>

    <!--======================
        CALLED NAMED TEMPLATES
        ======================-->

    <xsl:template name="id_type">
        <xsl:param name="fragment" />
        <field name="id">
            <xsl:value-of select="$fragment/myDocID" />
        </field>
        <field name="recordtype">marc</field>
    </xsl:template>

    <!-- codes: language / country of origin of publication -->
    <xsl:template name="lang_country">
        <xsl:param name="fragment" />
        <xsl:variable name="forDeduplication">
            <!-- remove undefined values (|||, und) from index -->
            <xsl:for-each select="$fragment/datafield[@tag='041']/subfield[@code='a']/text()">
                <xsl:choose>
                    <xsl:when test="matches(., '\|\|\||und')" />
                    <xsl:when test="string-length(.) &gt; 3" />
                    <xsl:otherwise>
                        <xsl:value-of select="concat(., '##xx##')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="$fragment/controlfield[@tag='008']">
                <xsl:variable name="lang" select="substring(text()[1],36,3)"/>
                <xsl:choose>
                    <xsl:when test="matches($lang, '\|\|\||und')" />
                    <xsl:otherwise>
                        <xsl:value-of select="concat($fragment/substring(controlfield[@tag='008'][1],36,3), '##xx##')" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>


        </xsl:variable>
        <!--<xsl:variable name="uniqueSeqValues" select="swissbib:startDeduplication($forDeduplication)"/>-->
        <xsl:call-template name="prepareDedup">
            <xsl:with-param name="fieldname" select="'language'" />
            <xsl:with-param name="fieldValues" select ="$forDeduplication" />
        </xsl:call-template>
        <xsl:variable name="forDeduplication">
            <xsl:for-each select="$fragment/controlfield[@tag='008']">
                <xsl:value-of select="concat(substring(text(),16,3), '##xx##')" />
            </xsl:for-each>
            <xsl:for-each select="$fragment/datafield[@tag='044']/subfield[@code='a']/text()">
                <xsl:value-of select="concat(., '##xx##')" />
            </xsl:for-each>
        </xsl:variable>
        <!--<xsl:variable name="uniqueSeqValues" select="swissbib:startDeduplication($forDeduplication)"/>-->
        <xsl:call-template name="prepareDedup">
            <xsl:with-param name="fieldname" select="'origcountry_isn_mv'"/>
            <xsl:with-param name="fieldValues" select="$forDeduplication"/>
        </xsl:call-template>
    </xsl:template>

    <!-- fetching TOC fulltext -->
    <xsl:template name="fulltext">
        <xsl:param name="fragment"/>
        <!-- reduziert (18.08.2011/osc) -->
        <xsl:variable name="di" select="$fragment/myDocID"/>
        <!-- ich muss hier noch etwas einbauen, dass das Feld nicht aufgebaut wird, wenn kein Text zurueckgeliefert wird -->
        <xsl:for-each select="$fragment/uri856">
            <xsl:variable name="url856" select="."/>

            <preparedfulltext><xsl:value-of select="$url856"/></preparedfulltext>

        </xsl:for-each>
        <xsl:for-each select="$fragment/uri956">
            <xsl:variable name="url956" select="."/>
            <preparedfulltext><xsl:value-of select="$url956"/></preparedfulltext>
        </xsl:for-each>
    </xsl:template>


    <xsl:template name="prepareDedup">
        <xsl:param name="fieldname"/>
        <xsl:param name="fieldValues"/>
        <xsl:element name="prepareddedup">
            <xsl:element name="field">
                <xsl:attribute name="name">
                    <xsl:value-of select="$fieldname"/>
                </xsl:attribute>
                <xsl:value-of select="$fieldValues"/>
            </xsl:element>
        </xsl:element>
    </xsl:template>



</xsl:stylesheet>
