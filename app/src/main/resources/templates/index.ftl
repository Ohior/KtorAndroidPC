<#-- @ftlvariable name="folders" type="kotlin.collections.List<com.example.ktorandroidpc.utils.dataclass>" -->
    <#import "_layout.ftl" as layout />
    <@layout.header>
    <#list folders?reverse as folders>
    <hr>
    <div>
        <h3>
            <a href="/articles/${article.id}">${folders.name}</a>
        </h3>
    </div>
</#list>
<hr>
<p>
    <a href="/articles/new">Create article</a>
</p>
</@layout.header>