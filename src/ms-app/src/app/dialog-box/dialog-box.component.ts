import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";

@Component({
  selector: 'app-dialog-box',
  templateUrl: './dialog-box.component.html',
  styleUrls: ['./dialog-box.component.css']
})
export class DialogBoxComponent implements OnInit {

  message_txt: string;
  state: number;
  
  constructor(
    private dialogRef: MatDialogRef<DialogBoxComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { 
    this.message_txt = data.message_txt;
    this.state = data.state;
    this.dialogRef.updatePosition({ top: '90px', left: '550px' });
    this.dialogRef.updateSize('650px','200px');
    this.dialogRef.disableClose = true;
  }

  ngOnInit() { }

  close() {
    this.dialogRef.close();
  }

}
