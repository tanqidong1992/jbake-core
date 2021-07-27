package com.tqd.flexmark.toc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.vladsch.flexmark.ast.Heading;

public class Toc {

    public List<TocItem> roots=new ArrayList<>();
    
    private transient TocItem lastAddItem;
    
    public boolean isEmpty(){
    	return CollectionUtils.isEmpty(roots);
    }
    public void print() {
    	roots.forEach(ti->{
    		ti.print();
    	});
    }
	public void addHeading(Heading h) {
		TocItem toBeAddItem=TocItem.fromHeading(h);
		if(lastAddItem==null) {
			roots.add(toBeAddItem);
		}else {
			if(toBeAddItem.level==lastAddItem.level) {
				if(lastAddItem.parent==null) {
					roots.add(toBeAddItem);
				}else {
					toBeAddItem.parent=lastAddItem.parent;
					lastAddItem.parent.children.add(toBeAddItem);
				}
			}else if (toBeAddItem.level>lastAddItem.level) {
				toBeAddItem.parent=lastAddItem;
				lastAddItem.children.add(toBeAddItem);
			}else if(toBeAddItem.level<lastAddItem.level) {
				TocItem parent=lastAddItem.parent;
				while(parent!=null && toBeAddItem.level<=parent.level) {
					parent=parent.parent;
				}
				if(parent==null) {
					roots.add(toBeAddItem);
				}else {
					toBeAddItem.parent=parent;
					parent.children.add(toBeAddItem);
				}
			}
		}
		lastAddItem=toBeAddItem;
	}
	public List<TocItem> getRoots() {
		return roots;
	}
	public void setRoots(List<TocItem> roots) {
		this.roots = roots;
	}
    
	
}
