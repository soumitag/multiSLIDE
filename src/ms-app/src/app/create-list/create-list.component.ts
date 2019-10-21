import { Component, OnInit, Input } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
  selector: 'app-create-list',
  templateUrl: './create-list.component.html',
  styleUrls: ['./create-list.component.css']
})
export class CreateListComponent implements OnInit {

  listnameStr: string;

  listNameErrMsg: string = "";
  listNameErr: boolean = false;
  
  constructor(private dialogRef: MatDialogRef<CreateListComponent>) {
      //this.analysis_name = data.analysis_name;
      this.dialogRef.updatePosition({ top: '110px', left: '700px' });
      this.dialogRef.updateSize('550px','220px');
  }

  ngOnInit() {
  }

  close() {
    this.close();
  }

  saveChanges() {
    if (this.listnameStr == '' || this.listnameStr == null) {
      this.listNameErr = true;
      this.listNameErrMsg = "A unique list name is required";
      return;
    }
    this.dialogRef.close(this.listnameStr);
  }

}
