import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NgModule } from '@angular/core';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { HeatmapComponent } from './heatmap/heatmap.component';

import { HeatmapService } from './heatmap.service';
import { DatauploadService } from './dataupload.service'
import { LoadNextComponent } from './load-next/load-next.component';
import { MapContainerComponent } from './map-container/map-container.component';
import { DataUploaderComponent } from './data-uploader/data-uploader.component';
import { CreateAnalysisComponent } from './create-analysis/create-analysis.component';
import { HistogramComponent } from './histogram/histogram.component'
import { HistogramService } from './histogram.service';
import { HomepageComponent } from './homepage/homepage.component';
import { RouterModule, Routes } from '@angular/router';
import { GetPreviewComponent } from './get-preview/get-preview.component';
import { CreateSessionService } from './create-session.service';
import { VisualizationHomeComponent } from './visualization-home/visualization-home.component';
import { SearchPanelComponent } from './search-panel/search-panel.component';
import { SelectionPanelComponent } from './selection-panel/selection-panel.component';
import { SearchService } from './search.service';
import { AssortedService } from './assorted.service';
import { AnalysisService } from './analysis.service'
import { NetworkNeighborhoodComponent } from './network-neighborhood/network-neighborhood.component';
import { LegendsComponent } from './legends/legends.component';

import { MenuPanelComponent } from './menu-panel/menu-panel.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {
  MatAutocompleteModule,
  MatBadgeModule,
  MatBottomSheetModule,
  MatButtonModule,
  MatButtonToggleModule,
  MatCardModule,
  MatCheckboxModule,
  MatChipsModule,
  MatDatepickerModule,
  MatDialogModule,
  MatDividerModule,
  MatExpansionModule,
  MatGridListModule,
  MatIconModule,
  MatInputModule,
  MatListModule,
  MatMenuModule,
  MatNativeDateModule,
  MatPaginatorModule,
  MatProgressBarModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatRippleModule,
  MatSelectModule,
  MatSidenavModule,
  MatSliderModule,
  MatSlideToggleModule,
  MatSnackBarModule,
  MatSortModule,
  MatStepperModule,
  MatTableModule,
  MatTabsModule,
  MatToolbarModule,
  MatTooltipModule,
  MatTreeModule,
} from '@angular/material';
import {CdkTableModule} from '@angular/cdk/table';
import {CdkTreeModule} from '@angular/cdk/tree';
import { ContextMenuComponent } from './context-menu/context-menu.component';
import { HierarchicalClusteringComponent } from './hierarchical-clustering/hierarchical-clustering.component';
import { NewListComponent } from './new-list/new-list.component';
import { ListService } from './list.service';
import { ListPanelComponent } from './list-panel/list-panel.component';
import { MapSettingsComponent } from './map-settings/map-settings.component';
import { CreateListComponent } from './create-list/create-list.component';
import { MetadataMapComponent } from './metadata-map/metadata-map.component';

const appRoutes: Routes = [
  { path: '', component: HomepageComponent },
  { path: 'create_analysis', component: CreateAnalysisComponent },
  { path: 'visualization_home', component: VisualizationHomeComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    HeatmapComponent,
    LoadNextComponent,
    MapContainerComponent,
    DataUploaderComponent,
    CreateAnalysisComponent,
    HistogramComponent,
    HomepageComponent,
    GetPreviewComponent,
    VisualizationHomeComponent,
    SearchPanelComponent,
    SelectionPanelComponent,
    NetworkNeighborhoodComponent,
    LegendsComponent,
    MenuPanelComponent,
    ContextMenuComponent,
    HierarchicalClusteringComponent,
    NewListComponent,
    ListPanelComponent,
    MapSettingsComponent,
    CreateListComponent,
    MetadataMapComponent
  ],
  imports: [
    RouterModule.forRoot(
      appRoutes,
      { enableTracing: true } // <-- debugging purposes only
    ), 
	  HttpModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatMenuModule,
    MatListModule,
    MatTooltipModule
  ],
  providers: [
    HeatmapService, 
    DatauploadService,
    HistogramService,
    CreateSessionService,
    SearchService,
    AssortedService,
    AnalysisService,
    ListService
  ],
  bootstrap: [AppComponent],
  exports: [
      CdkTableModule,
      CdkTreeModule,
      MatAutocompleteModule,
      MatBadgeModule,
      MatBottomSheetModule,
      MatButtonModule,
      MatButtonToggleModule,
      MatCardModule,
      MatCheckboxModule,
      MatChipsModule,
      MatStepperModule,
      MatDatepickerModule,
      MatDialogModule,
      MatDividerModule,
      MatExpansionModule,
      MatGridListModule,
      MatIconModule,
      MatInputModule,
      MatListModule,
      MatMenuModule,
      MatNativeDateModule,
      MatPaginatorModule,
      MatProgressBarModule,
      MatProgressSpinnerModule,
      MatRadioModule,
      MatRippleModule,
      MatSelectModule,
      MatSidenavModule,
      MatSliderModule,
      MatSlideToggleModule,
      MatSnackBarModule,
      MatSortModule,
      MatTableModule,
      MatTabsModule,
      MatToolbarModule,
      MatTooltipModule,
      MatTreeModule
    ],
  entryComponents: [
    NewListComponent,
    MapSettingsComponent,
    CreateListComponent,
    MetadataMapComponent,
    HierarchicalClusteringComponent
  ]
})
export class AppModule { }
