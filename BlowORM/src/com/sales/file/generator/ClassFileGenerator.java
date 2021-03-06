/**
  *  BLOW-ORM is an open source ORM for java and its currently under development.
  *
  *  Copyright (C) 2016  @author Divyank Sharma
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  
  *  In Addition to it if you find any bugs or encounter any issue you need to notify me.
  *  I appreciate any suggestions to improve it.
  *  @mailto: divyank01@gmail.com
  */
package com.sales.file.generator;

import java.util.Iterator;

import com.sales.blow.annotations.BlowId;
import com.sales.blow.annotations.BlowProperty;
import com.sales.blow.annotations.BlowSchema;
import com.sales.blow.annotations.One2Many;
import com.sales.blow.annotations.One2One;
import com.sales.poolable.parsers.ORM_MAPPINGS_Parser.ORM_MAPPINGS.Maps;
import com.sales.poolable.parsers.ORM_MAPPINGS_Parser.ORM_MAPPINGS.Maps.Attributes;
import com.sales.utils.BlowCoreUtils;

public class ClassFileGenerator {


	private String comment="/**\n"
						    +"* This File is generated by BlowORM according to the mapping provided\n"
						    +"* Modification in this file can lead to data curruption\n"
						    +"*/\n\n";
	
	public void createTheClass(Maps maps,StringBuilder builder){
		try{
			boolean one2one=false,one2many=false,property=false;
			builder.append("package "+getPackageName(maps.getClassName())+";\n\n");
			int afterPkg=builder.length();
			builder.append("import "+BlowSchema.class.getCanonicalName()+";\n");
			builder.append("\n@BlowSchema(schemaName=\""+maps.getSchemaName()+"\")");
			builder.append("\npublic class "+getClassName(maps.getClassName())+"{\n");
			Iterator<String> itr=maps.getAttributeMap().keySet().iterator();
			while(itr.hasNext()){
				String propName=itr.next();
				Attributes attr=maps.getAttributeMap().get(propName);
				if(attr.getType()==null && !isInSamePackage(maps,attr.getClassName())){
					builder.insert(afterPkg, "import "+attr.getClassName()+";\n");
				}
				if(attr.isPk()){
					builder.append("\n\t@BlowId"+(attr.isGenerated()?("(generated=true,seq="+attr.getSeqName()+")"):"")+"\n");
					builder.append("\t@BlowProperty(columnName=\""+attr.getColName()+"\",length="+attr.getLength()+")\n");
					if(!property){
						property=true;
						builder.insert(afterPkg, "import "+BlowProperty.class.getCanonicalName()+";\n");
					}
					builder.insert(afterPkg, "import "+BlowId.class.getCanonicalName()+";\n");
				}else if(attr.isFk()){
					if(attr.getCollectionType()==null){
						builder.append("\n\t@One2One(fk=\""+attr.getColName()+"\","+"isReferenced="+attr.isReferenced()+")\n");
						if(!one2one){
							one2one=true;
							builder.insert(afterPkg, "import "+One2One.class.getCanonicalName()+";\n");
						}
					}
					if(attr.getCollectionType()!=null){
						builder.append("\n\t@One2Many(fk=\""+attr.getColName()+"\","+"type=\""+attr.getClassName()+"\",collectionType=\""+(attr.getCollectionType().equals("list")?"java.util.List":"")+"\")\n");
						if(!one2many){
							one2many=true;
							builder.insert(afterPkg, "import "+One2Many.class.getCanonicalName()+";\n");
						}
						
						builder.append("\tprivate "+(attr.getCollectionType().equals("list")?"java.util.List<":"")+(attr.getType()!=null?attr.getType():getClassName(attr.getClassName()))+"> "+attr.getName()+";\n");
					}
				}else {
					builder.append("\n\t@BlowProperty(columnName=\""+attr.getColName()+"\",length="+attr.getLength()+")\n");
					if(!property){
						property=true;
						builder.insert(afterPkg, "import "+BlowProperty.class.getCanonicalName()+";\n");
					}
				}
				if(attr.getCollectionType()==null)
					builder.append("\tprivate "+(attr.getType()!=null?attr.getType():getClassName(attr.getClassName()))+" "+attr.getName()+";\n");
			}
			Iterator<String> itr1=maps.getAttributeMap().keySet().iterator();
			while(itr1.hasNext()){
				String propName=itr1.next();
				Attributes attr=maps.getAttributeMap().get(propName);
				createGetter(attr, builder);
				createSetter(attr, builder);
			}
			builder.append("\n}");
			builder.insert(afterPkg, comment);
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private String getPackageName(String filename){
		return filename.substring(0, filename.lastIndexOf("."));
	}

	private String getClassName(String filename){
		return filename.substring(filename.lastIndexOf(".")+1);
	}

	private boolean isInSamePackage(Maps maps,String toImport){
		if(getPackageName(maps.getClassName()).equals(getPackageName(toImport)))
			return true;
		else
			return false;
	}

	private void createGetter(Attributes attr,StringBuilder builder){
		builder.append("\tpublic "
				+(attr.getCollectionType()==null?
						attr.getType()!=null?attr.getType():getClassName(attr.getClassName()):
						(attr.getCollectionType().equals("list")?"java.util.List<":"")+(attr.getType()!=null?attr.getType():getClassName(attr.getClassName()))+">"
				 )
				+" "+BlowCoreUtils.getterFieldName(attr.getName())+"(){\n");
		builder.append("\t\treturn this."+attr.getName()+";\n");
		builder.append("\t}\n");
	}

	private void createSetter(Attributes attr,StringBuilder builder){
		builder.append("\tpublic void "+BlowCoreUtils.setterFieldName(attr.getName())+"("
				+(attr.getCollectionType()==null?
						attr.getType()!=null?attr.getType():getClassName(attr.getClassName()):
						(attr.getCollectionType().equals("list")?"java.util.List<":"")+(attr.getType()!=null?attr.getType():getClassName(attr.getClassName()))+">"
				 )
				+" "+attr.getName()+"){\n");
		builder.append("\t\tthis."+attr.getName()+"="+attr.getName()+";\n");
		builder.append("\t}\n");
	}
}
