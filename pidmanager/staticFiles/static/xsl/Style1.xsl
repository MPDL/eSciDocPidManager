<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method='html' version='1.0' encoding='UTF-8'
		indent='yes' />

	<xsl:template match="/">
		<xsl:apply-templates />
	</xsl:template>

	<!-- =============== Param ==================== -->
	<xsl:template match="param">
		<html>
			<head>
				<title>Param</title>
			</head>
			<body bgcolor="#DDDDDD">
				<div align="right">PIDManager</div>
				<hr noshadow="true" />
				<table border="0" width="60%">
					<tr>
						<td>
							<b>Element</b>
						</td>
						<td>
							<b>Value</b>
						</td>
					</tr>


					<xsl:for-each select="./*">
						<xsl:variable name="nodename" select="name()" />
						<xsl:variable name="nodevalue">
							<xsl:value-of select="." />
						</xsl:variable>
						<tr>
							<td>
								<xsl:value-of select="name()" />
							</td>
							<td>
								<xsl:choose>
									<!-- URL -->
									<xsl:when test="$nodename='url'">
										<xsl:choose>
											<!-- not entry -->
											<xsl:when
												test="$nodevalue='http://localhost/coldRun'">
												<font color="red">
													no real entry set
												</font>
												(
												<xsl:value-of
													select="$nodevalue" />
												)
											</xsl:when>
											<!-- real value -->
											<xsl:otherwise>
												<a
													href="{$nodevalue}">
													<xsl:value-of
														select="$nodevalue" />
												</a>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
									<!-- Other -->
									<xsl:otherwise>
										<xsl:value-of
											select="$nodevalue" />
									</xsl:otherwise>
								</xsl:choose>
							</td>
						</tr>
					</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>



	<!-- =============== Exception ==================== -->
	<xsl:template match="exception" name="exception">
		<html>
			<head>
				<title>
					Exception:
					<xsl:value-of select="title" />
				</title>
			</head>
			<body bgcolor="#DDDDDD">
				<div align="right">PIDManager</div>
				<hr noshadow="true" />
				<table border="0" width="60%">
					<tr>
						<td>Exception</td>
						<td>
							<font color="red" size="+2">
								<xsl:value-of select="title" />
							</font>
						</td>
					</tr>
					<tr>
						<td>Message</td>
						<td>
							<xsl:value-of select="message" />
						</td>
					</tr>
				</table>
			</body>
		</html>
	</xsl:template>

</xsl:stylesheet>