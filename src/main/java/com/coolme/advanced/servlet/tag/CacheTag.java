package com.coolme.advanced.servlet.tag;


import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * implements TagSupport{@link TagSupport} to set cache
 */
public class CacheTag extends TagSupport {


    private String pragma;
    private String cache_control;
    private Long expires;


    @Override
    public int doStartTag() throws JspException {

        HttpServletResponse response = (HttpServletResponse) pageContext.getResponse();
        if (cache_control != null) {
            response.setHeader("Cache-Control", (String) cache_control);
        }
        if (pragma != null) {
            response.setHeader("Pargma", pragma);
        }
        if (expires != null) {
            response.setHeader("Expires", String.valueOf(expires));
        }
        return SKIP_BODY;
    }

    public void setPragma(String pargma) {
        this.pragma = pargma;
    }

    public void setCache_control(String cache_control) {
        this.cache_control = cache_control;
    }

    public void setExpires(Long expires) {
        this.expires = expires;
    }
}
