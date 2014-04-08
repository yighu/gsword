<g:javascript library="application" />

<br>

Upload your source file: <br />
<br>
    <g:uploadForm action="upload">
        <input type="file" name="upfile" />
<br>
Select Bible Version
<select name="bv" class="books" id="bv" width="40">
<option value="ChiUns" >简体和合本圣经</option>
<option value="chiuntxin" >繁体和合本圣经</option>
<option value="ChiNCVs" >简体中文新译本</option>
<option value="ChiNCVt" >繁体中文新译本</option>
<option value="lzzbible" >吕振中译本圣经</option>
<option value="ESV" >English Standard Version</option>
<option value="BBE" >1949/1964 Bible in Basic English</option>
<option value="AKJV" >American King James Version</option>
<option value="ASV" >American Standard Version (1901)</option>
<option value="Byz" >Byzantine/Majority Text (2000)</option>
<option value="KJV" >King James Version</option>
<option value="RNKJV" >Restored Name King James Version</option>
<option value="LXX" >Septuagint, Morphologically Tagged Rahlfs'</option>
<option value="UKJV" >Updated King James Version</option>
<option value="YLT" >Young's Literal Translation (1898)</option>
</select>
<br>
        <input type="submit" />
    </g:uploadForm>
Download your file:
<a href="/gsword/transdoc/${fl}">${fl}</a>
<br/>
<br>
If you have English text file that quotes Bible verses, this service injects Chinese Bible text into text file. 
<br>
<br>

Please make sure English Bible reference is put into () like this:<p>
<br>
The word of God (john 3:16, genesis 1:3-5) 
<br>
<br>
<p>The file must be in ASCII format.<p>
<br>
The above line will be translated into the following:<p>
<br>
The word of God 神 说 ：要有 光 ，就有了 光 。 神 看 光 是好的 ，就把光 暗 分开了 。 神 称 光 为昼 ，称 暗 为夜 。有 晚上 ，有 早晨 ，这是头一 日 。 神 爱 世人 ，甚至 将他的 独生 子 赐给 他们，叫 一切 信 他的 ，不 至 灭亡 ，反 得 永 生 。(john 3:16, genesis 1:3-5)<p>
