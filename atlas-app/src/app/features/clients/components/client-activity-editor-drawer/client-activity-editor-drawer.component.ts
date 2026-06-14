import { Component, inject } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ClientActivityEditorFacade } from '../../services/client-activity-editor.facade';

@Component({
  selector: 'app-client-activity-editor-drawer',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './client-activity-editor-drawer.component.html',
  styleUrl: './client-activity-editor-drawer.component.scss',
})
export class ClientActivityEditorDrawerComponent {
  protected readonly clientActivityEditor = inject(ClientActivityEditorFacade);
}
