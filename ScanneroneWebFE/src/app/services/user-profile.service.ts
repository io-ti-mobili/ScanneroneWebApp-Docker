import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SimpleStatDto } from '../dto/simple-stat.dto';
import { ChartDataDto } from '../dto/chart-data.dto';
import { UserGeoCoverageDto } from '../dto/user-geo-coverage.dto';

@Injectable({
  providedIn: 'root'
})
export class UserProfileService {
  private baseUrl = '/api/users';

  constructor(private http: HttpClient) {}

  getMetrics(userId: number): Observable<SimpleStatDto[]> {
    return this.http.get<SimpleStatDto[]>(`${this.baseUrl}/${userId}/metrics`);
  }

  getTimelineUploads(userId: number): Observable<ChartDataDto[]> {
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/${userId}/charts/timeline/uploads`);
  }

  getTimelinePoints(userId: number): Observable<ChartDataDto[]> {
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/${userId}/charts/timeline/points`);
  }

  getSecurityDistribution(userId: number): Observable<ChartDataDto[]> {
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/${userId}/charts/security`);
  }

  getBandDistribution(userId: number): Observable<ChartDataDto[]> {
    return this.http.get<ChartDataDto[]>(`${this.baseUrl}/${userId}/charts/band`);
  }

  getGeoCoverage(userId: number): Observable<UserGeoCoverageDto[]> {
    return this.http.get<UserGeoCoverageDto[]>(`${this.baseUrl}/${userId}/geo-coverage`);
  }
}
