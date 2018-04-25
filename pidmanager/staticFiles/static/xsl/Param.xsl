<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method='html' version='1.0' encoding='UTF-8'
		indent='yes' />

	<xsl:template match="/">
		<html>
			<head>
				<title>Response:</title>
			</head>
			<body bgcolor="#DDDDDD">
				<div align="right">PIDManager</div>
				<hr noshadow="true" />
				<table border="0" width="60%">
					<xsl:for-each select="param">

						<tr>
							<td>Message</td>
							<td>
								<xsl:value-of
									select="exception/message" />
							</td>
						</tr>
					</xsl:for-each>


				</table>
			</body>
		</html>

	</xsl:template>

</xsl:stylesheet>