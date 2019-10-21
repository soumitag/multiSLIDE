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
import { AnalysisService } from './analysis.service';
import { ClusteringService } from './clustering.service';
import { SignificanceTestingService } from './significance-testing.service';
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
import { HierarchicalClusteringComponent } from './hierarchical-clustering/hierarchical-clustering.component';
import { NewListComponent } from './new-list/new-list.component';
import { ListService } from './list.service';
import { ListPanelComponent } from './list-panel/list-panel.component';
import { MapSettingsComponent } from './map-settings/map-settings.component';
import { CreateListComponent } from './create-list/create-list.component';
import { MetadataMapComponent } from './metadata-map/metadata-map.component';
import { UploadListComponent } from './upload-list/upload-list.component';
import { SaveVizComponent } from './save-viz/save-viz.component';
import { SignificanceTestingComponent } from './significance-testing/significance-testing.component';
import { PhenotypeSortingComponent } from './phenotype-sorting/phenotype-sorting.component';
import { SaveWorkspaceComponent } from './save-workspace/save-workspace.component';
import { CloseAnalysisComponent } from './close-analysis/close-analysis.component';
import { DialogBoxComponent } from './dialog-box/dialog-box.component';

const appRoutes: Routes = [
  { path: '', component: HomepageComponent },
  { path: 'create_analysis', component: CreateAnalysisComponent },
  { path: 'visualization_home', component: VisualizationHomeComponent },
  { path: 'save_viz', component: SaveVizComponent }
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
    HierarchicalClusteringComponent,
    NewListComponent,
    ListPanelComponent,
    MapSettingsComponent,
    CreateListComponent,
    MetadataMapComponent,
    UploadListComponent,
    SaveVizComponent,
    SignificanceTestingComponent,
    PhenotypeSortingComponent,
    SaveWorkspaceComponent,
    CloseAnalysisComponent,
    DialogBoxComponent
  ],
  imports: [
    RouterModule.forRoot(
      appRoutes,
      { enableTracing: true, useHash: true } // <-- debugging purposes only
    ), 
	  HttpModule,
    BrowserModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatMenuModule,
    MatListModule,
    MatTooltipModule,
    MatSlideToggleModule,
    MatRadioModule
  ],
  providers: [
    HeatmapService, 
    DatauploadService,
    HistogramService,
    CreateSessionService,
    SearchService,
    AssortedService,
    AnalysisService,
    ListService,
    ClusteringService,
    SignificanceTestingService
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
    HierarchicalClusteringComponent,
    NetworkNeighborhoodComponent,
    UploadListComponent,
    SignificanceTestingComponent,
    PhenotypeSortingComponent,
    SaveWorkspaceComponent,
    CloseAnalysisComponent,
    DialogBoxComponent
  ]
})
export class AppModule { }
