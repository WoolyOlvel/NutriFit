<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;

class Plan_list extends Model
{
    use HasFactory;

    protected $table = 'patient';

    protected $fillable = [
        'name',
        'image',
        'phone',
        'tipo',
        'tiempo'
    ];

    // 'Time' debe tratarse como objeto datetime
    protected $casts = [
        'time' => 'datetime',
    ];
}
