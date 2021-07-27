package com.tqd.flexmark.toc;

import java.util.ArrayList;
import java.util.List;

import com.vladsch.flexmark.ast.Heading;

public class TocItem {
    
	public String id;
	public String name;
	public int level;
	public transient TocItem parent;
	public List<TocItem> children=new ArrayList<>();
	
	public static TocItem fromHeading(Heading heading) {
		TocItem item=new TocItem();
		item.name=heading.getText().toString();
		item.level=heading.getLevel();
		item.id=heading.getAnchorRefId();
		return item;
	}
	
	public void print() {
		for(int i=0;i<level;i++) {
			System.out.print("*");
		}
		System.out.println(name);
		children.forEach(TocItem::print);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
 
	public List<TocItem> getChildren() {
		return children;
	}

	public void setChildren(List<TocItem> children) {
		this.children = children;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
}
