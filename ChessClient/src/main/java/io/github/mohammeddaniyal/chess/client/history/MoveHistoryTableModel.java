package io.github.mohammeddaniyal.chess.client.history;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
public class MoveHistoryTableModel extends AbstractTableModel
{
private String[] title={"Move no.","White","Black"};
private List<String> whiteMoves;
private List<String> blackMoves;
public MoveHistoryTableModel(byte firstTurnOfPlayer)
{
if(firstTurnOfPlayer==0)
{
title[1]="Black";
title[2]="White";
}
whiteMoves=new LinkedList<>();
blackMoves=new LinkedList<>();
}
public int getRowCount()
{
return Math.max(whiteMoves.size(),blackMoves.size());
}
public int getColumnCount()
{
return this.title.length;
}
public String getColumnName(int column)
{
return title[column];
}
public Object getValueAt(int row,int column)
{
if(column==0) return row+1;
if(column==1)
{ 
if(title[1].equals("White"))
{
if(this.whiteMoves.size()<this.blackMoves.size())
{
if(row>=this.whiteMoves.size()) return "";
}
}
else
{
if(this.whiteMoves.size()>this.blackMoves.size()) 
{
if(row==this.blackMoves.size()) return "";
}
}
List<String> list=(title[1].equals("White"))?this.whiteMoves:this.blackMoves;
if(list.size()==0) return "";
String data=list.get(row);
if(data==null) return "";
return data;
}

//for column 2

if(title[2].equals("White"))
{
if(this.whiteMoves.size()<this.blackMoves.size())
{
if(row>=this.whiteMoves.size()) return "";
}
}
else
{
if(this.whiteMoves.size()>this.blackMoves.size()) 
{
if(row>=this.blackMoves.size()) return "";
}
}

List<String> list=(title[2].equals("White"))?this.whiteMoves:this.blackMoves;
if(list.size()==0) return "";
String data=list.get(row);
if(data==null) return "";
return data;
}
public boolean isCellEditable(int row,int column)
{
return false;
}
public Class getColumnClass(int column)
{
return String.class;
}
public void addBlackMove(String move)
{
this.blackMoves.add(move);
fireTableDataChanged();
}
public void addWhiteMove(String move)
{
this.whiteMoves.add(move);
fireTableDataChanged();
}
}//class ends
