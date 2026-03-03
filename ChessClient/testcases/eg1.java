import io.github.mohammeddaniyal.chess.client.history.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
class MoveHistoryTable extends JFrame
{
MoveHistoryTable()
{
MoveHistoryTableModel mhtm=new MoveHistoryTableModel((byte)1);
JTable table=new JTable(mhtm);
JScrollPane scrollPane=new JScrollPane(table);
Container container=getContentPane();
setLayout(new BorderLayout());
JPanel panel=new JPanel();
panel.setLayout(new BorderLayout());
JTextField textField=new JTextField();
JButton blackButton=new JButton("Black");
JButton whiteButton=new JButton("White");
panel.add(textField,BorderLayout.SOUTH);
panel.add(blackButton,BorderLayout.WEST);
panel.add(whiteButton,BorderLayout.EAST);
blackButton.addActionListener((ev)->{
mhtm.addBlackMove(textField.getText().trim());
});
whiteButton.addActionListener((ev)->{
mhtm.addWhiteMove(textField.getText().trim());
});
container.add(scrollPane);
container.add(panel,BorderLayout.SOUTH);
setVisible(true);
setLocation(10,20);
setSize(400,400);
}
}
class psp
{
public static void main(String gg[])
{
new MoveHistoryTable();
}
}
