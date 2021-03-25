package com.darrenyuen.sardine.report;

import com.darrenyuen.sardine.model.Multistatus;
import com.darrenyuen.sardine.util.SardineUtil;

import java.io.IOException;

public abstract class SardineReport<T>
{
	public String toXml() throws IOException
	{
		return SardineUtil.toXml(toJaxb());
	}

	public abstract Object toJaxb();

	public abstract T fromMultistatus(Multistatus multistatus);
}
