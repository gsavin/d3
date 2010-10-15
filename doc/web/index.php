<?php $page = isset($_GET['page']) ? $_GET['page'] : 'home'; ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html;charset=UTF-8"/>
    <meta http-equiv="Content-Language" content="fr"/>

    <title>D3, Dynamic and Decentralized Distribution</title>

    <meta name="Author" content="Guilhelm Savin"/>
    <meta name="Keywords" content="guilhelm savin d3 distribution middleware "/>

    <meta name="Description" content=""/>
    <meta name="Robots" content="all"/>
    <meta name="revisit-after" content="30 days"/>
    
    <link rel="shortcut icon" href="/~savin/d3/img/favicon.png" />
    <link rel="stylesheet" type="text/css" href="/~savin/d3/d3.css"/>
    <link rel="stylesheet" type="text/css" href="/~savin/d3/rst.css"/>

    </style>
  </head>
  <body>
  	<div id="world">
  		<div id="top">
      		<div id="d3-logo"></div>
      		<div id="top-content">
      			<div style="font-family: Impact; font-size: 40px; line-height: 100%;"><span style="font-size:50px;">d</span>ynamic and<br/><span style="font-size:50px;">d</span>ecentralized<br/><span style="font-size:50px;">d</span>istribution</div>
      		</div>
      	</div>
      	<div id="menubar">
      	<a href="/~savin/d3/?page=home">Home</a> | <a href="/~savin/d3/?page=publication">Publications</a> | <a href="/~savin/d3/?page=manual">Manual</a> | <a href="/~savin/d3/api/index.html">API</a> | Downloads | <a href="http://github.com/gsavin/d3">d3@github</a>
      	</div>
      	<div id="content" style="clear: both;">
	<?php 
	  $pageFile = "content/$page.ih";
	  if( file_exists($pageFile) )
	    include($pageFile);
	?>
      	</div>
	<div id="footer">Copyright (C) 2010 - Guilhelm Savin ( guilhelm [dot] savin [at] litislab.fr )</div>
    </div>
    </div>
  </body>
</html>
