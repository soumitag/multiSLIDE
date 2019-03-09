import { Component, OnInit, ViewChild } from '@angular/core';
import { MatMenuTrigger } from '@angular/material/menu';
import { MatMenuModule } from '@angular/material/menu';
import { MatListModule } from '@angular/material/list'

@Component({
  selector: 'app-context-menu',
  templateUrl: './context-menu.component.html',
  styleUrls: ['./context-menu.component.css']
})
export class ContextMenuComponent implements OnInit {

  items = [
    {id: 1, name: 'Item 1'},
    {id: 2, name: 'Item 2'},
    {id: 3, name: 'Item 3'}
  ];

  //@ViewChild(MatMenuTrigger)
  //contextMenu: MatMenuTrigger;

  constructor() { }

  ngOnInit() {
  }

  //contextMenuPosition = { x: '0px', y: '0px' };

  /*
  onContextMenu(event: MouseEvent, item: Item) {
    event.preventDefault();
    this.contextMenuPosition.x = event.clientX + 'px';
    this.contextMenuPosition.y = event.clientY + 'px';
    this.contextMenu.menuData = { 'item': item };
    this.contextMenu.openMenu();
  }
  */

  onContextMenuAction1(item: string) {
    alert('Click on Action 1 for entrez = ' + item);
  }

  onContextMenuAction2(item: string) {
    alert('Click on Action 2 for entrez = ' + item);
  }
  
}

export interface Item {
  id: number;
  name: string;
}
